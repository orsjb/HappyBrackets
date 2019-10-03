package net.happybrackets.core.control;

import java.net.InetAddress;

abstract class DynamicControlParent {

    private DynamicControl control = null;

    /**
     * Removes all listeners from this object
     */
    public void removeListeners(){
        if (control != null)
        {
            control.eraseListeners();
        }
    }

    /**
     * Constructor
     * @param dynamicControl our control
     */
    protected DynamicControlParent(DynamicControl dynamicControl){
        control = dynamicControl;

        // Now ad a listener to call the abstract function
        control.addControlListener(control -> {
            notifyListener(control.getValue());
        });
    }

    /**
     * Register Listener to receive changed values in the control
     * @param listener Listener to register for events
     * @return this
     */
    public DynamicControlParent addControlListener(DynamicControl.DynamicControlListener listener)
    {
        control.addControlListener(listener);
        return this;
    }


    /**
     * Deregister listener so it no longer receives messages from this control
     * @param listener The lsitener we are removing
     * @return this object
     */
    public DynamicControlParent removeControlListener(DynamicControl.DynamicControlListener listener) {
        control.removeControlListener(listener);
        return  this;
    }



    /**
     * Register Listener to receive changed values in the control scope
     * @param listener Listener to register for events
     * @return this object
     */
    public DynamicControlParent addControlScopeListener(DynamicControl.ControlScopeChangedListener listener){
        control.addControlScopeListener(listener);
        return this;
    }

    /**
     * Deregister listener so it no longer receives messages from this control
     * @param listener the listener
     * @return this object
     */
    public DynamicControlParent removeControlScopeChangedListener(DynamicControl.ControlScopeChangedListener listener) {
        control.removeControlScopeChangedListener(listener);
        return this;
    }
    /**
     * Provide underlying access to DynamicControl object
     * @return the underlying DynamicControl object
     */
    public DynamicControl getDynamicControl(){
        return control;
    }

    /**
     * Add a device name or IP address as a target for {@link DynamicControl} messages.
     * The control must have {@link ControlScope#TARGET} scope to have any effect
     * @param device the device name or IP address to target
     */
    public void addControlTarget(String device){
        control.addTargetDevice(device);
    }

    /**
     * Add an {@link InetAddress} as a target for {@link DynamicControl} messages.
     * The control must have {@link ControlScope#TARGET} scope to have any effect
     * @param device the address of the target
     */
    public void addControlTarget(InetAddress device){
        control.addTargetDevice(device);
    }

    /**
     * Erase all target addresses for the {@link DynamicControl}
     */
    public void clearControlTargets(){
        control.clearTargetDevices();
    }

    /**
     * Remove a device name or IP address as a target for {@link DynamicControl} messages.
     * The control must have {@link ControlScope#TARGET} scope to have any effect
     * @param device the device name or IP address to target
     */
    public void removeControlTarget(String device){
        control.removeTargetDevice(device);
    }

    /**
     * Remove an {@link InetAddress} as a target for {@link DynamicControl} messages.
     * The control must have {@link ControlScope#TARGET} scope to have any effect
     * @param device the address of the target
     */
    public void removeControlTarget(InetAddress device){
        control.removeTargetDevice(device);
    }

    abstract void notifyListener(Object val);
}
