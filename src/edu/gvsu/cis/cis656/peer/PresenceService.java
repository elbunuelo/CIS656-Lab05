//----------------------------------------------------------------------
//
//  Filename: PresenceService.java
//  Description:
//
//  $Id:$
//
//----------------------------------------------------------------------
package edu.gvsu.cis.cis656.peer;

/**
 * @author Jonathan Engelsma
 */

/**
 * The abstract interface that is to implemented by a remote
 * presence edu.gvsu.cis.cis656.peer.  ChatClients will use this interface to
 * register themselves with the presence edu.gvsu.cis.cis656.peer, and also to
 * determine and locate other users who are available for chat
 * sessions.
 */
public interface PresenceService {

    /**
     * Register a edu.gvsu.cis.cis656.client with the presence service.
     * @param reg The information that is to be registered about a edu.gvsu.cis.cis656.client.
     */
    void register(RegistrationInfo reg) throws Exception;

    /**
     * Unregister a edu.gvsu.cis.cis656.client from the presence service.  Client must call this
     * method when it terminates execution.
     * @param userName The name of the user to be unregistered.
     */
    void unregister(String userName) throws Exception;

    /**
     * Lookup the registration information of another edu.gvsu.cis.cis656.client.
     * @param name The name of the edu.gvsu.cis.cis656.client that is to be located.
     * @return The RegistrationInfo info for the edu.gvsu.cis.cis656.client, or null if
     * no such edu.gvsu.cis.cis656.client was found.
     */
    RegistrationInfo lookup(String name) throws Exception;


    /**
     * Sets the user's presence status.
     * @param name The name of the user whose status is to be set.
     * @param status true if user is available, false otherwise.
     */
    void setStatus(String userName, boolean status) throws Exception;

    /**
     * Determine all users who are currently registered in the system.
     * @return An array of RegistrationInfo objects - one for each edu.gvsu.cis.cis656.client
     * present in the system.
     */
    RegistrationInfo[] listRegisteredUsers() throws Exception;
}