/*
 * 
 */
package wicket.contrib.webbeans.fields;

import wicket.contrib.webbeans.containers.BeanGridPanel;
import wicket.contrib.webbeans.model.BeanMetaData;
import wicket.contrib.webbeans.model.ElementMetaData;
import wicket.model.IModel;


/**
 * A Field that presents a property's bean in a panel, but 
 * the bean's properties do not have labels. For errors, the bean's properties use the 
 * parent property's label. This is useful for creating simple composite fields directly from
 * small beans. 
 *
 * @author Dan Syrstad
 */
public class BeanWithParentLabelField extends AbstractField
{
    /**
     * Construct a new BeanWithParentLabelField.
     *
     * @param id the Wicket id for the editor.
     * @param model the model.
     * @param metaData the meta data for the property.
     * @param viewOnly true if the component should be view-only.
     */
    public BeanWithParentLabelField(String id, IModel model, ElementMetaData metaData, boolean viewOnly)
    {
        super(id, model, metaData, viewOnly);
        
        BeanMetaData beanMetaData = metaData.createBeanMetaData(viewOnly);
        // Rename the bean's property labels to this property's label.
        for (ElementMetaData prop : beanMetaData.getDisplayedElements()) {
            if (!prop.isAction()) {
                prop.setLabel(metaData.getLabel());
            }
        }
        
        if (!viewOnly && model.getObject(this) == null) {
            // Create a blank instance for editing.
            model.setObject(this, metaData.createInstance() );
        }

        add( new BeanGridPanel("p", model, beanMetaData, null, false) );
    }
}
