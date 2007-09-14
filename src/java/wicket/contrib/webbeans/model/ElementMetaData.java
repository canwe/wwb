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
package wicket.contrib.webbeans.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import wicket.Component;
import wicket.contrib.webbeans.fields.ImageLabel;
import wicket.markup.html.basic.Label;
import wicket.util.lang.PropertyResolver;

/**
 * Represents the metadata for a single property of a bean or an action.
 */
public final class ElementMetaData extends MetaData implements Serializable
{
    public static final String PARAM_ELEMENT_TYPE = "elementType";
    public static final String PARAM_VIEW_ONLY = "viewOnly";
    public static final String PARAM_LABEL_IMAGE = "labelImage";
    public static final String PARAM_LABEL = "label";
    public static final String PARAM_FIELD_TYPE = "fieldType";

    public static final int DEFAULT_ORDER = Integer.MAX_VALUE;
    
    private BeanMetaData beanMetaData;
    private String propertyName;
    private Class propertyType;
    private int order;
    private String tabId;
    private boolean isAction = false;
    // If true, the viewOnly parameter was set explicitly by beanprops. 
    private boolean viewOnlySetExplicitly = false;
    
    ElementMetaData(BeanMetaData beanMetaData, String propertyName, String label, Class propertyType)
    {
        this.beanMetaData = beanMetaData;
        this.propertyName = propertyName;
        this.propertyType = propertyType;
        setLabel(label);
        this.order = DEFAULT_ORDER;
        
        consumeParameter(PARAM_FIELD_TYPE);
        consumeParameter(PARAM_LABEL);
        consumeParameter(PARAM_LABEL_IMAGE);
        consumeParameter(PARAM_VIEW_ONLY);
        consumeParameter(PARAM_ELEMENT_TYPE);
    }
    
    /**
     * Applies parameters from a beanprops file to this element.
     *
     * @param params
     */
    void applyBeanProps(List<Parameter> params)
    {
        for (Parameter param : params) {
            List<ParameterValue> values = param.getValues();
            if (values.size() != 1) {
                throw new RuntimeException("Parameter " + param.getName() + "on element " + propertyName + " on bean " + beanMetaData.getBeanClass().getSimpleName() + " does not specify exactly one value.");
            }
            
            String value = values.get(0).getValue(beanMetaData.getComponent());
            setParameter(param.getName(), value);
            if (param.getName().equals(PARAM_VIEW_ONLY)) {
                viewOnlySetExplicitly = true;
            }
        }
    }
    
    public String getFieldType()
    {
        String fieldType = getParameter(PARAM_FIELD_TYPE);
        if (fieldType != null && fieldType.indexOf('.') < 0) {
            // Base class name provided. Try to resolve it from the BeanMetaData's ComponentRegistry.
            String fullFieldType = beanMetaData.getComponentRegistry().findMatchingFieldClass(fieldType);
            if (fullFieldType != null) {
                // Store it now that we know it.
                setFieldType(fullFieldType);
                return fullFieldType; 
            }
        }
        
        return fieldType;
    }
    
    public void setFieldType(String fieldType)
    {
        getParameters().setProperty(PARAM_FIELD_TYPE, fieldType);
    }

    public String getTabId()
    {
        return tabId;
    }
    
    public void setTabId(String groupId)
    {
        this.tabId = groupId;
    }

    public String getLabel()
    {
        return getParameter(PARAM_LABEL);
    }
    
    public void setLabel(String label)
    {
        getParameters().setProperty(PARAM_LABEL, label);
    }
    
    /**
     * Gets the labelImage.
     *
     * @return the labelImage.
     */
    public String getLabelImage()
    {
        return getParameter(PARAM_LABEL_IMAGE);
    }

    /**
     * @return the Component used to render the label. If a label image was specified, this is the
     *  image, otherwise a plain-text label. 
     */
    public Component getLabelComponent(String wicketId)
    {
        if (getLabelImage() == null) {
            return new Label(wicketId, getLabel());
        }

        return new ImageLabel(wicketId, getBeanMetaData().getComponent().getClass(), getLabelImage(), getLabel());
    }
    
    public int getOrder()
    {
        return order;
    }
    
    public void setOrder(int order)
    {
        this.order = order;
    }
    
    public String getPropertyName()
    {
        return propertyName;
    }
    
    public void setPropertyName(String propertyName)
    {
        this.propertyName = propertyName;
    }
    
    public boolean isViewOnly()
    {
        return getBooleanParameter(PARAM_VIEW_ONLY);
    }
    
    public void setViewOnly(boolean viewOnly)
    {
        getParameters().setProperty(PARAM_VIEW_ONLY, String.valueOf(viewOnly));
    }
    
    @Override
    public int hashCode()
    {
        int result = 1;
        result = 31 * result + ((propertyName == null) ? 0 : propertyName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) {
            return true;
        }
        
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        
        final ElementMetaData other = (ElementMetaData) obj;
        if (propertyName == null) {
            if (other.propertyName != null) {
                return false;
            }
        }

        return propertyName.equals(other.propertyName);
    }
    
    public Class getPropertyType()
    {
        return propertyType;
    }

    /**
     * Gets the beanMetaData.
     *
     * @return a BeanMetaData.
     */
    public BeanMetaData getBeanMetaData()
    {
        return beanMetaData;
    }

    public String getElementTypeName()
    {
        String elementType = getParameter(PARAM_ELEMENT_TYPE);
        if (elementType != null && elementType.indexOf('.') < 0) {
            // Base class name provided. Assume that it is the bean's package.
            String beanPkg = beanMetaData.getBeanClass().getPackage().getName();
            if (beanPkg.length() > 0) {
                elementType = beanPkg + '.' + elementType;
                setParameter(PARAM_ELEMENT_TYPE, elementType);
            }
        }
        
        return elementType;
    }
    
    /**
     * Gets the array/collection element type of the property. If elementTypeName is 
     * not defined and the property type is an array, the element type of the array is returned.
     * If elementTypeName is 
     * not defined and value is not null and it is
     * a Collection and is not empty, the element type is derived from the first element of the collection. 
     *
     * @param value the property's current value. May be null to ignore runtime type detection.
     * 
     * @return the elementType or null if not defined.
     */
    public Class getElementType(Object value)
    {
        Class elementType = null;
        String elementTypeName = getElementTypeName();
        if (elementTypeName == null) {
            // This only returns the element type if the property is an array, otherwise null.
            elementType = getPropertyType().getComponentType();
        }
        
        if (elementType == null) {
            // Try to detect it from first element if non-empty.
            if (value instanceof Collection) {
                Iterator iter = ((Collection)value).iterator();
                if (iter.hasNext()) {
                    elementType = iter.next().getClass();
                }
                else {
                    // If empty - just use Object.
                    elementType = new Serializable() { }.getClass();
                }
            }
        }

        if (elementType == null && elementTypeName != null) {
            try {
                return Class.forName(elementTypeName);
            }
            catch (ClassNotFoundException e) {
                throw new RuntimeException("Cannot load class for elementType: " + elementTypeName + " for property " + getPropertyName(), e);
            }
        }
        
        return elementType;
    }

    /**
     * Determines whether this element is an action.
     *
     * @return true if this is an action, else false.
     */
    public boolean isAction()
    {
        return isAction;
    }

    /**
     * Sets whether this element is an action.
     *
     * @param isAction true if this is an action, else false.
     */
    public void setAction(boolean isAction)
    {
        this.isAction = isAction;
    }
    
    /**
     * If this is an action, return the action's method name. 
     *
     * @return the action method name, or null if this is not an action.
     */
    public String getActionMethodName()
    {
        if (isAction()) {
            return getPropertyName().substring(BeanMetaData.ACTION_PROPERTY_PREFIX.length());
        }
        
        return null;
    }

    /**
     * Gets the value for this property from the given bean.
     *
     * @param bean the bean, which may be null.
     * 
     * @return the property's value, or null if bean is null.
     */
    public Object getPropertyValue(Object bean)
    {
        if (bean == null) {
            return null;
        }
        
        return PropertyResolver.getValue(getPropertyName(), bean);
    }

    /**
     * Creates a new BeanMetaData based on this property's type.
     * 
     * @param viewOnly
     * 
     * @return a new BeanMetaData.
     */
    public BeanMetaData createBeanMetaData(boolean viewOnly)
    {
        return createBeanMetaData(getPropertyType(), viewOnly);
    }
    
    /**
     * Creates a new BeanMetaData for the given beanType based on this property. Context and ComponentRegistry are
     * inherited from this element's BeanMetaData (the parent).
     * 
     * @param beanType
     * @param viewOnly
     * 
     * @return a new BeanMetaData.
     */
    public BeanMetaData createBeanMetaData(Class beanType, boolean viewOnly)
    {
        // Compose a keyPrefix from the original bean meta data, the base bean class name, and this property name.
        BeanMetaData parentMetaData = getBeanMetaData();
            
        return new BeanMetaData(beanType, parentMetaData.getContext(), parentMetaData.getComponent(),
                        parentMetaData.getComponentRegistry(), viewOnly, true);
    }
    
    /**
     * @return a new instance of the object represented by this property's type.
     */
    public Object createInstance()
    {
        // TODO - handle arrays???
        try {
            return getPropertyType().newInstance();
        }
        catch (Exception e) {
            throw new RuntimeException("Error creating instance of " + getPropertyType());
        }
    }

    /**
     * @return true if viewOnly was set explicitly in beanprops.
     */
    public boolean isViewOnlySetExplicitly()
    {
        return viewOnlySetExplicitly;
    }
}
