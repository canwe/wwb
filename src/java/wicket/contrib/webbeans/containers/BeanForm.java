/*---
   Copyright 2006-2007 Visual Systems Corporation.
   http://www.vscorp.com

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
   
        http://www.apache.org/licenses/LICENSE-2.0
   
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
---*/
package wicket.contrib.webbeans.containers;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import wicket.Component;
import wicket.MarkupContainer;
import wicket.ajax.AjaxRequestTarget;
import wicket.ajax.IAjaxCallDecorator;
import wicket.ajax.form.AjaxFormValidatingBehavior;
import wicket.behavior.AbstractBehavior;
import wicket.behavior.IBehavior;
import wicket.behavior.SimpleAttributeModifier;
import wicket.contrib.webbeans.actions.BeanActionButton;
import wicket.contrib.webbeans.fields.AbstractField;
import wicket.contrib.webbeans.fields.Field;
import wicket.contrib.webbeans.model.BeanMetaData;
import wicket.contrib.webbeans.model.BeanPropertyModel;
import wicket.contrib.webbeans.model.ElementMetaData;
import wicket.contrib.webbeans.model.TabMetaData;
import wicket.extensions.markup.html.tabs.AbstractTab;
import wicket.extensions.markup.html.tabs.TabbedPanel;
import wicket.markup.ComponentTag;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.basic.Label;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.FormComponent;
import wicket.markup.html.form.HiddenField;
import wicket.markup.html.form.SubmitLink;
import wicket.markup.html.list.ListItem;
import wicket.markup.html.list.ListView;
import wicket.markup.html.panel.FeedbackPanel;
import wicket.markup.html.panel.Panel;
import wicket.model.Model;
import wicket.model.PropertyModel;

/**
 * Generic component for presenting a bean form. Supports the following parameter: <p>
 * <ul>
 * <li>label - the form's label.</li>
 * </ul>
 *
 * @author Dan Syrstad
 */
public class BeanForm extends Panel
{
    private static Logger logger = Logger.getLogger(BeanForm.class.getName());
    private static final long serialVersionUID = -7287729257178283645L;

    private Form form;
    private FormVisitor formVisitor;
    private FeedbackPanel feedback;
    // Wicket ID/HTML ID of field with focus.
    private String focusField = null;
    private BeanPropertyChangeListener listener = new BeanPropertyChangeListener();
    /** Tracks beans for which we have registered listeners. Key is the bean (by identity) and 
     * value is the listener registered on the bean. */
    private IdentityHashMap<Object, BeanPropertyChangeListener> registeredListeners = new IdentityHashMap<Object, BeanPropertyChangeListener>();

    /** Maps components in this form to their properties. */
    private Set<ComponentPropertyMapping> componentPropertyMappings = new HashSet<ComponentPropertyMapping>(200);
    /** Components that should be refreshed on the new Ajax Component update. */ 
    private Set<ComponentPropertyMapping> refreshComponents = new HashSet<ComponentPropertyMapping>(200);
    /** Form submit recursion counter. Zero means we're not validating currently. */
    private int submitCnt = 0;
    
    /**
     * Construct a new BeanForm.
     *
     * @param id the Wicket id for the panel.
     * @param bean the bean to be displayed.
     * @param beanMetaData the meta data for the bean
     */
    public BeanForm(String id, final Object bean, final BeanMetaData beanMetaData)
    {
        super(id);
        
        form = new Form("f") {
            // Track whether the form is in submit processing.
            public boolean process()
            {
                ++submitCnt;
                try {
                    return super.process();
                }
                finally {
                    --submitCnt;
                }
            }
        };
        
        form.setOutputMarkupId(true);
        add(form);
        
        String title = beanMetaData.getLabel();
        form.add( new Label("title", title) );
        
        final HiddenField focusField = new HiddenField("focusField", new PropertyModel(this, "focusField"));
        focusField.add( new AbstractBehavior() {
            public void onComponentTag(Component component, ComponentTag tag)
            {
                tag.put("id", "bfFocusField");
                super.onComponentTag(component, tag);
            }
        });
        
        form.add(focusField);
        
        formVisitor = new FormVisitor();
        
        List<TabMetaData> tabMetaDataList = beanMetaData.getTabs();
        if (tabMetaDataList.get(0).getId().equals(BeanMetaData.DEFAULT_TAB_ID)) {
            // Single default tab - none explicitly specified. Don't add a tab panel.
            form.add( new BeanGridPanel("tabs", bean, beanMetaData, tabMetaDataList.get(0)) );
        }
        else {
            List<AbstractTab> tabs = new ArrayList<AbstractTab>();
            for (final TabMetaData groupMetaData : tabMetaDataList) {
                tabs.add( new AbstractTab( new Model(groupMetaData.getLabel()) ) {
                    public Panel getPanel(String panelId)
                    {
                        return new BeanGridPanel(panelId, bean, beanMetaData, groupMetaData);
                    }
                } );
            }
    
            // This is a tabbed panel that submits the form and doesn't switch if there are errors. 
            TabbedPanel tabbedPanel = new TabbedPanel("tabs", tabs) {
                protected WebMarkupContainer newLink(String linkId, final int index)
                {
                    return new TabbedPanelSubmitLink(linkId, index, this);
                }
            };
            
            form.add(tabbedPanel);
        }
        
        feedback = new FeedbackPanel("feedback");
        feedback.setOutputMarkupId(true);
        form.add(feedback);        

        // Add bean actions.
        List<ElementMetaData> globalActions = beanMetaData.getGlobalActions();
        form.add(new ListView("actions", globalActions) {
            protected void populateItem(ListItem item)
            {
                ElementMetaData element = (ElementMetaData)item.getModelObject();
                item.add( new BeanActionButton("action", element, form, bean) );
            }
        });
    }
    
    /**
     * Finds the BeanForm that is the parent of the given childComponent.
     *
     * @param childComponent the child, may be null.
     * 
     * @return the parent BeanForm, or null if childComponent is not part of a BeanForm.
     */
    public static BeanForm findBeanFormParent(Component childComponent)
    {
        if (childComponent == null) {
            return null;
        }
        
        return (BeanForm)childComponent.visitParents(BeanForm.class, new IVisitor() {
            public Object component(Component visited)
            {
                return (BeanForm)visited;
            }
        });
    }
    
    /**
     * Determines if the BeanForm associated with childComponent is currently in a form
     * submit phase.
     * 
     * @param childComponent the child, may be null.
     * 
     * @return true if the BeanForm is validating, or false if not.
     */
    public static boolean isInSubmit(Component childComponent)
    {
        BeanForm beanForm = findBeanFormParent(childComponent);
        if (beanForm != null) {
            return beanForm.submitCnt > 0;
        }
        
        return false;
    }
    
    /**
     * Registers the given component with this form. This is usually called by Fields
     * (for example, see {@link AbstractField}) to add the form behavior to their
     * components.
     * 
     * @param component
     */
    public void registerComponent(Component component, Object bean, ElementMetaData element)
    {
        if (bean != null) {
            ComponentPropertyMapping mapping = new ComponentPropertyMapping(bean, element);
            componentPropertyMappings.add(mapping);
            
            if (!registeredListeners.containsKey(bean)) {
                // Listen for PropertyChangeEvents on this bean, if necessary.
                // TODO When do we unregister?? Maybe a WeakRef to ourself in the listener? Then listener unregisters
                // TODO if we don't exist anymore.
                element.getBeanMetaData().addPropertyChangeListener(bean, listener);
                registeredListeners.put(bean, listener);
            }
        }
        
        if (component instanceof MarkupContainer) {
            ((MarkupContainer)component).visitChildren(formVisitor);
        }
        else {
            component.add(new FormSubmitter("onchange"));
        }
    }
    
    /**
     * Allows external app to set the field to receive focus.
     * 
     * @param component the component, may be null to unset the field.
     */
    public void setFocusComponent(Component component)
    {
        setFocusField( component == null ? null : component.getId() );
    }
    
    /**
     * Gets the focusField.
     *
     * @return the focusField.
     */
    public String getFocusField()
    {
        return focusField;
    }

    /**
     * Sets the focusField.
     *
     * @param focusField the focusField to set.
     */
    public void setFocusField(String focusField)
    {
        this.focusField = focusField;
    }

    /**
     * Refresh the targetComponent, in addition to any components that need to be updated
     * due to property change events.
     *
     * @param target
     * @param targetComponent the targetComponent, may be null.
     */
    public void refreshComponents(final AjaxRequestTarget target, Component targetComponent)
    {
        if (targetComponent != null) {
            refreshComponent(target, targetComponent);
        }
        
        if (!refreshComponents.isEmpty()) {
            // Refresh components fired from our PropertyChangeListener.
            
            // Visit all children and see if they match the fired events. 
            form.visitChildren( new IVisitor() {
                public Object component(Component component)
                {
                    Object model = component.getModel();
                    if (model instanceof BeanPropertyModel) {
                        BeanPropertyModel propModel = (BeanPropertyModel)model;
                        ElementMetaData componentMetaData = propModel.getElementMetaData();
                        for (ComponentPropertyMapping mapping : refreshComponents) {
                            if (mapping.elementMetaData == componentMetaData) {
                                refreshComponent(target, component);
                                break;
                            }
                        }
                    }

                    return IVisitor.CONTINUE_TRAVERSAL;
                }
            });

            refreshComponents.clear();
        }
    }

    private void refreshComponent(final AjaxRequestTarget target, Component targetComponent)
    {
        // Refresh this field. We have to find the parent Field to do this.
        MarkupContainer field;
        if (targetComponent instanceof Field) {
            field = (MarkupContainer)targetComponent;
        }
        else {
            field = targetComponent.findParent(AbstractField.class);
        }
        
        if (field != null) {
            if (!field.getRenderBodyOnly()) {
                target.addComponent(field);
            }
            else {
                // Field is RenderBodyOnly, have to add children individually
                field.visitChildren( new IVisitor() {
                    public Object component(Component component)
                    {
                        if (!component.getRenderBodyOnly()) {
                            target.addComponent(component);
                        }
                        
                        return IVisitor.CONTINUE_TRAVERSAL;
                    }
                    
                });
            }
        }
        else {
            target.addComponent(targetComponent);
        }
    }

    /**
     * Monitors tab panel submits. 
     */
    private final class TabbedPanelSubmitLink extends SubmitLink
    {
        private final int index;
        private final TabbedPanel panel;

        private TabbedPanelSubmitLink(String id, int index, TabbedPanel panel)
        {
            super(id, form);
            this.index = index;
            this.panel = panel;
        }

        @Override
        protected void onSubmit()
        {
            if (panel.getSelectedTab() != index) {
                panel.setSelectedTab(index);
                // TODO this could remember last focus field on the tab and refocus when switching back to the tab
                // TODO Keep separate tab array of focus fields?
                setFocusField(null);
            }

            refreshComponents.clear();
        }
    }

    private final class FormVisitor implements IVisitor, Serializable
    {
        public Object component(Component component) 
        {
            if (component instanceof FormComponent) {
                boolean addBehavior = true;
                for (IBehavior behavior : (List<IBehavior>)component.getBehaviors()) {
                    if (behavior instanceof FormSubmitter) {
                        addBehavior = false;
                        break;
                    }
                }
                
                if (addBehavior) {
                    FormSubmitter behavior = new FormSubmitter("onchange");
                    // Note: Do NOT set a delay. The delay can cause an onchange to be sent AFTER a button submit
                    // which causes the submit button's messages to be erased. <- That was true when we used AjaxSubmitButtons, we don't anymore.
                    //behavior.setThrottleDelay(Duration.milliseconds(250));
                    component.add(behavior);
                    component.add( new SimpleAttributeModifier("onfocus", "bfOnFocus(this)") );
                }
            }
            
            return IVisitor.CONTINUE_TRAVERSAL;
        }
    }

    private final class FormSubmitter extends AjaxFormValidatingBehavior implements Serializable
    {
        private FormSubmitter(String event)
        {
            super(form, event);
        }

        @Override
        protected void onSubmit(final AjaxRequestTarget target)
        {
            /*
            // NOTE: The following code fails to clear off field errors that have been corrected.
             
            // Only refresh messages if we have one. Otherwise previous error messages go away on the
            // first field change.
            if (form.getPage().hasFeedbackMessage()) {
                super.onSubmit(target);
            }
            */
            super.onSubmit(target);
            refreshComponents(target, getComponent() );
        }

        @Override
        protected void onError(AjaxRequestTarget target)
        {
            super.onError(target);
            refreshComponents(target, getComponent() );
        }
        
        @Override
        protected IAjaxCallDecorator getAjaxCallDecorator()
        {
            return AjaxBusyDecorator.INSTANCE;
        }
    }
    
    public static final class AjaxBusyDecorator implements IAjaxCallDecorator
    {
        public static final AjaxBusyDecorator INSTANCE = new AjaxBusyDecorator();

        public CharSequence decorateOnFailureScript(CharSequence script)
        {
            return "bfIndicatorError();" + script;
        }

        public CharSequence decorateOnSuccessScript(CharSequence script)
        {
            return "bfIndicatorOff();" + script;
        }

        public CharSequence decorateScript(CharSequence script)
        {
            return "bfIndicatorOn(); " + script;
        }
    }
    
    /**
     * Simple data structure for mapping components and properties. <p>
     */
    private static final class ComponentPropertyMapping implements Serializable
    {
        Object bean;
        ElementMetaData elementMetaData;
        
        ComponentPropertyMapping(Object bean, ElementMetaData elementMetaData)
        {
            this.bean = bean;
            this.elementMetaData = elementMetaData;
        }

        /** 
         * {@inheritDoc}
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode()
        {
            int result = 31 + ((bean == null) ? 0 : bean.hashCode());
            result = 31 * result + ((elementMetaData == null) ? 0 : elementMetaData.hashCode());
            return result;
        }

        /** 
         * {@inheritDoc}
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj)
        {
            if (!(obj instanceof ComponentPropertyMapping)) {
                return false;
            }

            final ComponentPropertyMapping other = (ComponentPropertyMapping)obj;
            return bean == other.bean && 
                    (elementMetaData == other.elementMetaData || 
                     (elementMetaData != null && elementMetaData.equals(other.elementMetaData)));
        }
    }
    
    /**
     * Listens to property change events on a bean and adds them to the queue of
     * components to be refreshed. <p>
     */
    private final class BeanPropertyChangeListener implements PropertyChangeListener, Serializable
    {
        public void propertyChange(PropertyChangeEvent evt)
        {
            // Find matching component
            Object bean = evt.getSource();
            String propName = evt.getPropertyName();
            for (ComponentPropertyMapping mapping : componentPropertyMappings) {
                if (bean == mapping.bean && propName.equals(mapping.elementMetaData.getPropertyName())) {
                    BeanForm.this.refreshComponents.add(mapping);
                }
            }
        }
    }
}
