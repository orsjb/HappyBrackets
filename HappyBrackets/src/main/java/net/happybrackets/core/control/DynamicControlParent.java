package net.happybrackets.core.control;

import java.net.InetAddress;

/**
 * The {@link DynamicControlParent} facilitates a simplified abstract API to implement the {@link DynamicControl} class.
 *
 * The values can be represented as sliders, text boxes, check boxes, and buttons
 *
 * <br><br>A message can either be: <br> an integer - {@link IntegerControl},
 * <br> a double - {@link FloatControl},
 * <br> a string - {@link TextControl},
 * <br> a Boolean - {@link BooleanControl},
 * <br> a trigger - {@link TriggerControl}, or
 * <br> a complete class - {@link ClassObjectControl}.
 *
 */
public abstract class DynamicControlParent {

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
     * Get the name of the device that sent the message. If the message was local, will return this device name
     * @return name of device that sent message
     */
    public String getSendingDevice(){
        return control.getSendingDevice();
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
     * Add one or more device names or IP address as a target for {@link DynamicControl} messages.
     * The control must have {@link ControlScope#TARGET} scope to have any effect
     * @param devices the device name or IP address to target
     */
    public void addControlTarget(String... devices){
        control.addTargetDevice(devices);
    }

    /**
     * Clear existing targets and add one or more device names or IP address as a target for {@link DynamicControl} messages.
     * The control must have {@link ControlScope#TARGET} scope to have any effect
     * @param devices the device name or IP address to target
     */
    public void setControlTarget(String... devices){
        control.setTargetDevice(devices);
    }


    /**
     * Add one or more {@link InetAddress} as a target for {@link DynamicControl} messages.
     * The control must have {@link ControlScope#TARGET} scope to have any effect
     * @param deviceAddresses the addresses of the targets
     */
    public void addControlTarget(InetAddress... deviceAddresses){
        control.addTargetDevice(deviceAddresses);
    }

    /**
     * Clear existing targets and add one or more {@link InetAddress} as a target for {@link DynamicControl} messages.
     * The control must have {@link ControlScope#TARGET} scope to have any effect
     * @param deviceAddresses the addresses of the targets
     */
    public void setControlTarget(InetAddress... deviceAddresses){
        control.setTargetDevice(deviceAddresses);
    }

    /**
     * Erase all target addresses for the {@link DynamicControl}
     */
    public void clearControlTargets(){
        control.clearTargetDevices();
    }

    /**
     * Remove one or more device name or IP address as a target for {@link DynamicControl} messages.
     * The control must have {@link ControlScope#TARGET} scope to have any effect
     * @param devices the device names or IP addresses to remove as targets
     */
    public void removeControlTarget(String... devices){
        control.removeTargetDevice(devices);
    }

    /**
     * Remove one or more {@link InetAddress} as a target for {@link DynamicControl} messages.
     * The control must have {@link ControlScope#TARGET} scope to have any effect
     * @param inetAddresses the addresses of the targets to remove
     */
    public void removeControlTarget(InetAddress... inetAddresses){
        control.removeTargetDevice(inetAddresses);
    }

    /**
     * Changed the {@link ControlScope} the object has has. It will update control map so the correct events will be generated based on its scope
     * @param new_scope The new Control Scope
     * @return this object
     */
    public DynamicControlParent setControlScope(ControlScope new_scope){
        getDynamicControl().setControlScope(new_scope);
        return this;
    }

    abstract void notifyListener(Object val);
}
