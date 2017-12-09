package edu.gvsu.cis.cis656.peer;

import edu.gvsu.cis.cis656.chord.ChordClient;

/**
 * Concrete implementation of the PresenceService interface.
 * <p>
 * Uses an HTTP edu.gvsu.cis.cis656.client to access the REST gae edu.gvsu.cis.cis656.peer.
 */
public class Peer implements PresenceService {
    ChordClient client;

    public Peer() {
        client = new ChordClient();
    }


    @Override
    public void register(RegistrationInfo reg) throws Exception {
        client.put(reg.getUserName(), reg);

    }

    @Override
    public void unregister(String userName) throws Exception {
        RegistrationInfo reg = client.get(userName);
        if (reg != null) {
            client.remove(userName);
        }

    }

    @Override
    public RegistrationInfo lookup(String name) throws Exception {
        return client.get(name);
    }

    @Override
    public void setStatus(String userName, boolean status) throws Exception {
        RegistrationInfo reg = client.get(userName);
        if (reg != null) {
            client.remove(userName);
            reg.setStatus(status);
            client.put(userName, reg);
        }
    }

    @Override
    public RegistrationInfo[] listRegisteredUsers() throws Exception {
        return null;
    }
}
