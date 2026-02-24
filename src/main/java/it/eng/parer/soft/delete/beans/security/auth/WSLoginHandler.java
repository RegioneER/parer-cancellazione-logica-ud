/*
 * Engineering Ingegneria Informatica S.p.A.
 *
 * Copyright (C) 2023 Regione Emilia-Romagna
 * <p/>
 * This program is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Affero General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package it.eng.parer.soft.delete.beans.security.auth;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.util.SubnetUtils;

import it.eng.parer.soft.delete.beans.security.exception.AuthWSException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;

@SuppressWarnings("unchecked")
@ApplicationScoped
public class WSLoginHandler {

    EntityManager entityManager;

    private static final String USERNAME_PARAM = "username";
    //
    private static final String LOGIN_FALLITO_MSG = "Autenticazione fallita";
    private static final String LOGIN_IP_FALLITO_MSG = "Indirizzo IP dell'utente che ha originato la richiesta non autorizzato";
    //
    private static final String AUTH_QUERY = " SELECT 1 FROM IamUser iu JOIN iu.iamAbilOrganizs iao JOIN iao.iamAutorServs ias  WHERE iu.nmUserid = :username "
            + " AND iao.idOrganizApplic = :idOrganiz AND ias.nmServizioWeb = :servizioWeb";
    private static final String LOGIN_QUERY = " SELECT iu.cdPsw, iu.cdSalt, iu.flAttivo, iu.dtScadPsw, iu.flContrIp  FROM IamUser iu  WHERE iu.nmUserid = :username";
    private static final String IP_LIST_QUERY = " SELECT iiiu.cdIndIpUser FROM IamIndIpUser iiiu WHERE iiiu.iamUser.nmUserid = :username";

    @Inject
    public WSLoginHandler(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Controllo di autorizzazione di un utente, per un servizio in una organizzazione
     *
     * @param username    nome utente
     * @param idOrganiz   id dell'organizzazione
     * @param servizioWeb nome del servizio web
     *
     * @return true/false con verifica autorizzazione
     *
     * @throws AuthWSException eccezione lanciata se l'utente non è autorizzato
     */
    public boolean checkAuthz(String username, Integer idOrganiz, String servizioWeb)
            throws AuthWSException {
        Query q2 = entityManager.createQuery(AUTH_QUERY);
        q2.setParameter(USERNAME_PARAM, username);
        q2.setParameter("idOrganiz", new BigDecimal(idOrganiz));
        q2.setParameter("servizioWeb", servizioWeb);
        return doCheckAuthz(username, idOrganiz, servizioWeb, q2);
    }

    private boolean doCheckAuthz(String username, Integer idOrganiz, String servizioWeb, Query q2)
            throws AuthWSException {
        List<String> result = q2.getResultList();
        if (result == null || result.isEmpty()) {
            String exceptionMessage = "Utente " + username
                    + " non autorizzato ad eseguire il servizio " + servizioWeb;
            if (idOrganiz != null) {
                exceptionMessage = exceptionMessage + " all'interno dell'organizzazione con ID "
                        + idOrganiz;
            }
            throw new AuthWSException(AuthWSException.CodiceErrore.UTENTE_NON_AUTORIZZATO,
                    exceptionMessage);
        }
        return true;
    }

    /**
     * Controllo di autenticazione per un utente tramite la password / IP In caso di autenticazione
     * con token (OAuth2) viene estratto lo userid + controllo (eventuale) di IP
     *
     * @param username  nome utente
     * @param password  password
     * @param ipAddress indirizzo ip
     *
     * @return true/false con verifica autorizzazione
     *
     * @throws AuthWSException eccezione lanciata se l'utente non è autorizzato
     */
    public boolean login(String username, String password, String ipAddress)
            throws AuthWSException {
        Query q1 = entityManager.createQuery(LOGIN_QUERY);
        q1.setParameter(USERNAME_PARAM, username);
        Query ipListQuery = entityManager.createQuery(IP_LIST_QUERY);
        ipListQuery.setParameter(USERNAME_PARAM, username);
        return doLogin(username, password, ipAddress, q1, ipListQuery);
    }

    @SuppressWarnings("removal")
    private boolean doLogin(String username, String password, String ipAddress, Query q1,
            Query ipListQuery) throws AuthWSException {
        Object[] res;
        try {
            res = (Object[]) q1.getSingleResult();
        } catch (NoResultException e) {
            throw new AuthWSException(AuthWSException.CodiceErrore.LOGIN_FALLITO,
                    LOGIN_FALLITO_MSG);
        }
        if (res != null) {
            String salt = (String) res[1];
            String dbPwd = (String) res[0];
            boolean userAttivo = ((String) res[2]).equals("1");
            LocalDateTime expDate = (LocalDateTime) res[3];
            boolean ipCheck = ((String) res[4]).equals("1");
            if (!userAttivo) {
                throw new AuthWSException(AuthWSException.CodiceErrore.UTENTE_NON_ATTIVO,
                        "Utente non attivo");
            }
            if (LocalDateTime.now().isAfter(expDate)) {
                throw new AuthWSException(AuthWSException.CodiceErrore.UTENTE_SCADUTO,
                        "Credenziali scadute in data "
                                + expDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            }
            // aggiunto questo check in quanto se utilizzato il token (OAuth2)
            // non è necessario verificare la password
            if (StringUtils.isNoneBlank(password)) {
                if (StringUtils.isEmpty(salt)) {
                    if (!PwdUtil.encodePassword(password).equals(dbPwd)) {
                        throw new AuthWSException(AuthWSException.CodiceErrore.LOGIN_FALLITO,
                                LOGIN_FALLITO_MSG);
                    }
                } else {
                    if (!PwdUtil
                            .encodePBKDF2Password(PwdUtil.decodeUFT8Base64String(salt), password)
                            .equals(dbPwd)) {
                        throw new AuthWSException(AuthWSException.CodiceErrore.LOGIN_FALLITO,
                                LOGIN_FALLITO_MSG);
                    }
                }
            }
            if (ipCheck) {
                List<String> list = ipListQuery.getResultList();
                for (String ipAddr : list) {
                    if (ipAddr.contains("/")) {
                        SubnetUtils utils = new SubnetUtils(ipAddr);
                        if (utils.getInfo().isInRange(ipAddress)) {
                            return true;
                        }
                    } else {
                        if (ipAddr.equals(ipAddress)) {
                            return true;
                        }
                    }
                }
                throw new AuthWSException(AuthWSException.CodiceErrore.LOGIN_FALLITO,
                        LOGIN_IP_FALLITO_MSG);
            }
            return true;
        } else {
            throw new AuthWSException(AuthWSException.CodiceErrore.LOGIN_FALLITO,
                    LOGIN_FALLITO_MSG);
        }
    }
}
