package gnu.classpath.jdwp.transport;

import gnu.classpath.jdwp.JNodeSocketTransport;

/**
 *
 */
class NativeTransportFactory {
    private static Class getTransportClass(){
        return JNodeSocketTransport.class;
    }
}
