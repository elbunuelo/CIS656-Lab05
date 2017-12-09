package edu.gvsu.cis.cis656.chord;
/**
 * Some sample OpenChord code.  See OpenChord manual and javadocs for more detail.
 *
 * @author Jonathan Engelsma
 */

import de.uniba.wiai.lspi.chord.data.URL;
import de.uniba.wiai.lspi.chord.service.Chord;
import de.uniba.wiai.lspi.chord.service.PropertiesLoader;
import de.uniba.wiai.lspi.chord.service.ServiceException;
import de.uniba.wiai.lspi.chord.service.impl.ChordImpl;
import edu.gvsu.cis.cis656.peer.RegistrationInfo;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.Set;

public class ChordClient {
    private static final String MASTERHOST = "localhost";
    private static final String PORT = "64000";

    Chord chord;

    public ChordClient() {
        PropertiesLoader.loadPropertyFile();

        this.chord = new ChordImpl();
        try {
            this.joinNetwork();
        } catch (Exception e) {
            try {
                this.createNetwork();
            } catch (Exception e1) {
                throw new RuntimeException(e1);
            }

        }

    }

    public void createNetwork() throws ServiceException {
        String protocol = URL.KNOWN_PROTOCOLS.get(URL.SOCKET_PROTOCOL);
        URL localURL = null;
        try {
            localURL = new URL(protocol + "://" + MASTERHOST + ":" + PORT + "/");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        this.chord = new ChordImpl();
        this.chord.create(localURL);

    }

    public void joinNetwork() throws ServiceException {
        String protocol = URL.KNOWN_PROTOCOLS.get(URL.SOCKET_PROTOCOL);
        URL localURL = null;
        try {
            int randomPort = (int) (Math.random() * 5535) + 60000;
            localURL = new URL(protocol + "://" + InetAddress.getLocalHost().getHostAddress() + ":" + randomPort + "/");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        URL bootstrapURL = null;
        try {
            bootstrapURL = new URL(protocol + "://" + MASTERHOST + ":" + PORT + "/");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        this.chord.join(localURL, bootstrapURL);
    }

    public void put(String key, Serializable dataObject) {
        StringKey dataKey = new StringKey(key);
        try {
            this.chord.insert(dataKey, dataObject);
        } catch (ServiceException e) {
            e.printStackTrace();
        }
    }

    public RegistrationInfo get(String key) {
        StringKey dataKey = new StringKey(key);
        RegistrationInfo data = null;

        try {
            Set<Serializable> vals = chord.retrieve(dataKey);
            Iterator<Serializable> it = vals.iterator();
            while (it.hasNext()) {
                data = (RegistrationInfo) it.next();
            }
        } catch (ServiceException e) {
            e.printStackTrace();
        }

        return data;
    }

    public void remove(String key) {
        StringKey dataKey = new StringKey(key);
        try {
            Set<Serializable> vals = chord.retrieve(dataKey);
            Iterator<Serializable> it = vals.iterator();
            while (it.hasNext()) {
                this.chord.remove(dataKey, it.next());
            }
        } catch (ServiceException e) {
            e.printStackTrace();
        }
    }

    public void remove(String key, RegistrationInfo registrationInfo) {
        StringKey dataKey = new StringKey(key);
        try {
            this.chord.remove(dataKey, registrationInfo);
        } catch (ServiceException e) {
            e.printStackTrace();
        }
    }
}
