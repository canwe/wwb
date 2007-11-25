package net.sourceforge.wicketwebbeans.examples.customfields;

import net.sourceforge.wicketwebbeans.containers.BeanForm;
import net.sourceforge.wicketwebbeans.model.BeanMetaData;
import net.sourceforge.wicketwebbeans.model.ComponentRegistry;

import org.apache.wicket.markup.html.WebPage;

public class CustomFieldPage extends WebPage
{
    public CustomFieldPage()
    {
        Address bean = new Address();
        
        // Register the ModelField for the Country class.
        ComponentRegistry registry = new ComponentRegistry();
        registry.register(Country.class, CountryField.class);
        
        BeanMetaData meta = new BeanMetaData(bean.getClass(), null, this, registry, false);
        add( new BeanForm("beanForm", bean, meta) );
    }
}
