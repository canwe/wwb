package wicket.contrib.webbeans.examples.css;

import wicket.ajax.AjaxRequestTarget;
import wicket.contrib.webbeans.containers.BeanForm;
import wicket.contrib.webbeans.model.BeanMetaData;
import wicket.contrib.webbeans.model.ElementMetaData;
import wicket.contrib.webbeans.model.api.JBean;
import wicket.contrib.webbeans.model.api.JBeans;
import wicket.contrib.webbeans.model.api.JProperty;
import wicket.markup.html.WebPage;
import wicket.markup.html.form.Form;

public class CssPage extends WebPage
{
    public CssPage()
    {
        TestBean bean = new TestBean();
        
        // Create the meta data
        JBeans jbeans = new JBeans(
            new JBean(TestBean.class)
                .columns(1)
                .css("greenBeanBorder")
                .propertyNames("firstName", "lastName", "rows")
                .properties( new JProperty("firstName").css("purpleColor") ),
            new JBean(RowBean.class)
                .css("redBeanBorder") 
                .dynamicCss("getRowCss")
                .properties( new JProperty("description").dynamicCss("getDescriptionCss") )
        );
        
        BeanMetaData meta = new BeanMetaData(bean.getClass(), null, jbeans, this, null, false);
        add( new BeanForm("beanForm", bean, meta) );
    }

    public void save(AjaxRequestTarget target, Form form, TestBean bean)
    {
        info("Saved");
    }
    
    public String getRowCss(RowBean bean, BeanMetaData beanMetaData)
    {
        if (bean.getAmount() > 0) {
            return "greaterThanZero";
        }
        
        if (bean.getAmount() < 0) {
            return "lessThanZero";
        }
        
        return null;
    }

    public String getDescriptionCss(RowBean bean, ElementMetaData elementMetaData)
    {
        if (bean.getAmount() > 0) {
            return "fieldPositive";
        }
        
        if (bean.getAmount() < 0) {
            return "fieldNegative";
        }
        
        return null;
    }
}
