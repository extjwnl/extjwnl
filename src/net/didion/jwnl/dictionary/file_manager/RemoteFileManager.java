package net.didion.jwnl.dictionary.file_manager;

import net.didion.jwnl.util.MessageLog;
import net.didion.jwnl.util.MessageLogLevel;

import java.io.IOException;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * An object of this class can serve as a <code>FileManager</code> for remote <code>FileBackedDictionary</code>
 * instantiations using RMI. This class also contains utility routines to publish a <code>RemoteFileManager</code>
 * for remote use, and to lookup a remote one for local use.
 * <p/>
 * To make a <CODE>RemoteFileManager</CODE> available to remote clients:
 * <PRE>
 * System.setSecurityManager(new RMISecurityManager());
 * LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
 * new RemoteFileManager().bind();
 * </PRE>
 * <p/>
 * To create a local <CODE>Dictionary</CODE> backed by a remote <CODE>RemoteFileManager</CODE>:
 * <PRE>
 * Dictionary dictionary = new FileBackedDictionary(RemoteFileManager.lookup(hostname));
 * </PRE>
 */
public class RemoteFileManager extends FileManagerImpl {
    private static final MessageLog _log = new MessageLog(RemoteFileManager.class);
    /**
     * The standard RMI binding name.
     */
    public static final String BINDING_NAME = "jwnl";

    /**
     * Construct a file manager backed by a set of files contained in the default WN search directory.
     * See {@link FileManagerImpl} for a description of the default search directory.
     *
     * @throws RemoteException If remote operation failed.
     */
    public RemoteFileManager(String searchDir, Class dictionaryFileType) throws IOException, RemoteException {
        super(searchDir, dictionaryFileType);
        UnicastRemoteObject.exportObject(this);
    }

    /**
     * Bind this object to the value of <code>BINDING_NAME</code> in the local RMI
     * registry.
     *
     * @throws AlreadyBoundException If <code>BINDING_NAME</code> is already bound.
     * @throws RemoteException       If remote operation failed.
     */
    public void bind() throws RemoteException, AlreadyBoundException {
        _log.log(MessageLogLevel.INFO, "DICTIONARY_INFO_001", BINDING_NAME);
        Registry registry = LocateRegistry.getRegistry();
        registry.bind(BINDING_NAME, this);
    }

    /**
     * Lookup the object bound to the value of <code>BINDING_NAME</code> in the RMI
     * registry on the host named by <var>hostname</var>
     *
     * @return An RMI proxy of type <code>FileManager</code>.
     * @throws AccessException      If this operation is not permitted.
     * @throws NotBoundException    If there is no object named <code>BINDING_NAME</code> in the remote registry.
     * @throws RemoteException      If remote operation failed.
     * @throws UnknownHostException If the host could not be located.
     */
    public static FileManager lookup(String hostname) throws AccessException, NotBoundException, RemoteException, UnknownHostException {
        Registry registry = LocateRegistry.getRegistry(hostname);
        return (FileManager) registry.lookup(BINDING_NAME);
    }
}