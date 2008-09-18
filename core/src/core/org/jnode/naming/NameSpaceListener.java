package org.jnode.naming;


public interface NameSpaceListener<T> {
    /**
     * Method called when a service is bound with the namespace
     * @param service
     */
    public void serviceBound(T service);

    /**
     * Method called when a service is unbound with the namespace
     * @param service
     */
    public void serviceUnbound(T service);
}
