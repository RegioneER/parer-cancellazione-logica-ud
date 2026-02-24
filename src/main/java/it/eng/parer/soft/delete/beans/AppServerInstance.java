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

package it.eng.parer.soft.delete.beans;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.EnumMap;
import java.util.Enumeration;
import java.util.Map;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.arc.Lock;
import io.quarkus.arc.Lock.Type;
import io.quarkus.runtime.LaunchMode;
import io.quarkus.runtime.Startup;
import it.eng.parer.soft.delete.beans.exceptions.AppGenericRuntimeException;
import it.eng.parer.soft.delete.beans.utils.Costanti.ErrorCategory;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Lock
@Singleton
@Startup
public class AppServerInstance {

    private static final Logger log = LoggerFactory.getLogger(AppServerInstance.class);
    String servername = null;

    LaunchMode mode;

    @ConfigProperty(name = "quarkus.uuid")
    String instanceUUID;

    @Inject
    public AppServerInstance(LaunchMode mode) {
        this.mode = mode;
    }

    @PostConstruct
    protected void initSingleton() {
        try {
            log.atInfo().log("Inizializzazione singleton AppServerInstance...");
            InetAddress address = this.getMyHostAddress();

            log.atInfo().log("Indirizzo IP del server: {}", address.getHostAddress());
            log.atInfo().log("        Nome del server: {}", address.getCanonicalHostName());

            log.atInfo().log("Instance UUID: {}", instanceUUID);

            servername = address.getCanonicalHostName() + "/" + instanceUUID;
            log.atInfo().log("Il nome completo dell'istanza in esecuzione è {}", servername);

        } catch (UnknownHostException ex) {
            throw new AppGenericRuntimeException(ex, ErrorCategory.INTERNAL_ERROR);
        }
    }

    @Lock(value = Type.READ)
    public String getName() {
        return servername;
    }

    enum AddressTypes {
        // l'ordine di scelta dovrebbe essere quello che segue:
        // priorità ai nomi host, poi gli ipv4, poi gli ipv6.
        // nell'elenco sono presenti anche i due tipi di indirizzi di loopback
        // così che nel caso pessimo sia reso preferibilmente
        // l'indirizzo IPV4. (nel caso di questi due ultimi tipi di
        // indirizzo il nome host non è critico)
        // tendenzialmente è meglio un site local di un ip pubblico.
        // per capire se un ip ha un nome, verifico se questo è diverso dalla
        // rappresentazione in stringa delll'ip. (non molto bello, in effetti)
        SITE_LOCAL_WITH_NAME, NON_SITE_LOCAL_WITH_NAME, SITE_LOCAL_WITHOUT_NAME_IPV4,
        NON_SITE_LOCAL_WITHOUT_NAME_IPV4, SITE_LOCAL_WITHOUT_NAME_IPV6,
        NON_SITE_LOCAL_WITHOUT_NAME_IPV6, LOOPBACK_IPV4, LOOPBACK_IPV6
    }

    private InetAddress getMyHostAddress() throws UnknownHostException {
        try {
            Map<AddressTypes, InetAddress> map = new EnumMap<>(AddressTypes.class);

            // Nota se falliscono tutti questi tentativi la macchina è
            // probabilmente disconnessa dalla rete
            //
            // Scorri su tutte le interfacce di rete
            for (Enumeration<?> ifaces = NetworkInterface.getNetworkInterfaces(); ifaces
                    .hasMoreElements();) {
                NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
                // Scorri su tutti gli indirizzi IP associati ad una scheda di rete
                for (Enumeration<InetAddress> inetAddrs = iface.getInetAddresses(); inetAddrs
                        .hasMoreElements();) {
                    InetAddress inetAddr = inetAddrs.nextElement();
                    log.atDebug().log("Verifico l'indirizzo {}", inetAddr.getHostAddress());
                    AddressTypes tipo = decodeAddrType(inetAddr);
                    // se è già presente un indirizzo di questo tipo (per
                    // esempio se
                    // ci sono più schede di rete fisiche) lo sovrascrivo. Di
                    // fatto prendo
                    // l'ultimo che leggo.
                    map.put(tipo, inetAddr);
                }
            }

            // l'enum viene letto nell'ordine in cui è stato dichiarato,
            // garantendo la preferenza nella scelta del tipo di indirizzo reso
            for (AddressTypes at : AddressTypes.values()) {
                if (map.get(at) != null) {
                    log.atInfo().log("Ho selezionato l'indirizzo di tipo {}", at);
                    return map.get(at);
                }
            }

            // A questo punto, non siamo riusciti a determinare un indirizzo plausibile
            // Ripieghiamo usando l'API del JDK sperando che il risultato non sia
            // del tutto inutile (sotto Linux la cosa è frequente)
            InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
            if (jdkSuppliedAddress == null) {
                throw new UnknownHostException(
                        "Il metodo JDK InetAddress.getLocalHost() ha reso null.");
            }
            return jdkSuppliedAddress;
        } catch (Exception e) {
            UnknownHostException unknownHostException = new UnknownHostException(
                    "Impossibile determinare un indirizzo per la macchina: " + e);
            unknownHostException.initCause(e);
            throw unknownHostException;
        }
    }

    private AddressTypes decodeAddrType(InetAddress inetAddress) {
        AddressTypes tipo;
        if (!inetAddress.isLoopbackAddress()) {
            if (inetAddress.isSiteLocalAddress()) {
                if (isAddressWithHostName(inetAddress)) {
                    tipo = AddressTypes.SITE_LOCAL_WITH_NAME;
                } else if (inetAddress instanceof Inet4Address) {
                    tipo = AddressTypes.SITE_LOCAL_WITHOUT_NAME_IPV4;
                } else {
                    tipo = AddressTypes.SITE_LOCAL_WITHOUT_NAME_IPV6;
                }
            } else {
                if (isAddressWithHostName(inetAddress)) {
                    tipo = AddressTypes.NON_SITE_LOCAL_WITH_NAME;
                } else if (inetAddress instanceof Inet4Address) {
                    tipo = AddressTypes.NON_SITE_LOCAL_WITHOUT_NAME_IPV4;
                } else {
                    tipo = AddressTypes.NON_SITE_LOCAL_WITHOUT_NAME_IPV6;
                }
            }
            log.atDebug().log("è un indirizzo {}", tipo);
        } else {
            // non mi interessa se questo indirizzo di loopback ha un nome:
            // nella maggior parte dei casi si chiama "localhost".
            // questa discriminazione viene fatta solo per dare
            // priorità nella selezione all'indirizzio ipv4
            // rispetto a quello ipv6
            if (inetAddress instanceof Inet4Address) {
                tipo = AddressTypes.LOOPBACK_IPV4;
            } else {
                tipo = AddressTypes.LOOPBACK_IPV6;
            }
            log.atDebug().log("è un indirizzo di loopback di tipo {}", tipo);
        }
        return tipo;
    }

    private boolean isAddressWithHostName(InetAddress inetAddress) {
        return !inetAddress.getHostName().equals(inetAddress.getHostAddress());
    }

}
