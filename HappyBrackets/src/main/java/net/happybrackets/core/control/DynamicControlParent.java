package net.happybrackets.core.control;

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
     * Changed the scope that the control has. It will update control map so the correct events will be generated based on its scope
     * We must do this in subclass
     * @param new_scope The new Control Scope
     * @return this object
     */
    public DynamicControlParent setControlScope(ControlScope new_scope){
        control.setControlScope(new_scope);
        return this;

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

    abstract void notifyListener(Object val);
}
