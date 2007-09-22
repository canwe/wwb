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
/*
 * 
 */
package wicket.contrib.webbeans.fields;

import wicket.behavior.SimpleAttributeModifier;
import wicket.contrib.webbeans.containers.BeanForm;
import wicket.contrib.webbeans.model.BeanPropertyModel;
import wicket.contrib.webbeans.model.ElementMetaData;
import wicket.markup.html.form.FormComponent;
import wicket.markup.html.panel.Panel;
import wicket.model.IModel;
import wicket.util.string.Strings;


/**
 * Base class for Fields. In general, Fields should <em>not</em> set renderBodyOnly to true
 * if they want to be refreshed via Ajax.
 *
 * @author Dan Syrstad
 */
abstract public class AbstractField extends Panel implements Field
{
    private static final long serialVersionUID = -5452855853289381110L;
    
    private ElementMetaData elementMetaData;

    /**
     * Construct a AbstractField. 
     *
     * @param id the Wicket id for the editor.
     * @param model the model.
     * @param metaData the meta data for the property.
     * @param viewOnly true if the component should be view-only.
     */
    public AbstractField(String id, IModel model, ElementMetaData metaData, boolean viewOnly)
    {
        super(id, model);
        this.elementMetaData = metaData;
        
        metaData.consumeParameter(ElementMetaData.PARAM_REQUIRED);
        metaData.consumeParameter(ElementMetaData.PARAM_MAX_LENGTH);
        
        // Allow for refreshing of the field via Ajax.
        setOutputMarkupId(true);
        setRenderBodyOnly(false);
    }
    
    /**
     * @return true if the field's metadata says that it is a required field.
     */
    public boolean isRequiredField()
    {
        return elementMetaData.isRequired();
    }
    
    /**
     * @return the maximum length for the field, or null if no maxmimum length.
     */
    public Integer getMaxLength()
    {
        return elementMetaData.getMaxLength();
    }
    
    /**
     * @return the default string value for this field, or null if none is defined.
     */
    public String getDefaultValue()
    {
        return elementMetaData.getDefaultValue();
    }
    
    protected void setFieldParameters(FormComponent field)
    {
        Integer maxLength = getMaxLength();
        if (maxLength != null) {
            field.add( new SimpleAttributeModifier("maxlength", maxLength.toString()) );
        }
        
        String defaultValue = getDefaultValue();
        if (defaultValue != null && Strings.isEmpty(getModelObjectAsString())) {
            field.setModelValue(defaultValue);
        }
    }
    
    /**
     * {@inheritDoc}
     * @see wicket.Component#onAttach()
     */
    @Override
    protected void onAttach()
    {
        super.onAttach();
        // If we're part of a BeanForm, register ourself with it.
        BeanForm beanForm  = (BeanForm)findParent(BeanForm.class);
        if (beanForm != null) {
            beanForm.registerComponent(this, (BeanPropertyModel)getModel(), elementMetaData);
        }
    }

    /**
     * Gets the elementMetaData.
     *
     * @return a ElementMetaData.
     */
    public ElementMetaData getElementMetaData()
    {
        return elementMetaData;
    }

    /**
     * Returns the ElementMetaData related to the specified propertyName.
     * @param metaData metaData for this field.
     * @param parameterName the parameter name on metaData. The value of this parameter should be the property name to lookup.
     * @param propertyClass the class type the property should resolve to
     * @return ElementMetaData
     */
    protected ElementMetaData getDependentProperty(ElementMetaData metaData, String parameterName, Class propertyClass) {
        ElementMetaData property = null;
        String propStr = metaData.getParameter(parameterName);
        if (propStr != null) {
            property = metaData.getBeanMetaData().findElement(propStr);
            if (property == null) {
                throw new RuntimeException("'" + propStr + "' is not defined on " + metaData.getBeanMetaData().getBeanClass());
            }

            if (!propertyClass.isAssignableFrom( property.getPropertyType() )) {
                throw new RuntimeException(parameterName + "'" + propStr + "' must return a " + propertyClass.getName() + " on "
                                + metaData.getBeanMetaData().getBeanClass() + ". Instead it returns "
                                + property.getPropertyType());
            }
        }
        return property;
    }

    /**
     * Gets the dependent property bean from the specified property.
     *
     * @return the dependent property bean, or null if not defined.
     * @param property the ElementMetaData property to extract the bean from
     */
    protected Object getDependentPropertyBean(ElementMetaData property)
    {
        if (property != null) {
            BeanPropertyModel model = (BeanPropertyModel)getModel();
            return property.getPropertyValue( model.getBean() );
        }

        return null;
    }
}
