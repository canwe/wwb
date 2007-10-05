package wicket.contrib.webbeans.examples.complex;


import java.util.ArrayList;
import java.util.List;

import wicket.ajax.AjaxRequestTarget;
import wicket.contrib.webbeans.containers.BeanForm;
import wicket.contrib.webbeans.model.BeanMetaData;
import wicket.markup.html.WebPage;
import wicket.markup.html.form.Form;

public class ExpPage2 extends WebPage
{
    private TestBean bean = new TestBean();
    private BeanForm beanForm;
    
    public ExpPage2()
    {
        List<TestBean2> beans = new ArrayList<TestBean2>();
        beans.add( new TestBean2("Dan", "Syrstad") );
        beans.add( new TestBean2("Joe", "Smith") );
        for (int i = 0; i < 25; i++) {
            beans.add( new TestBean2("Row", String.valueOf(i)) );
        }
        bean.setBeans(beans);
        
        BeanMetaData meta = new BeanMetaData(bean.getClass(), null, this, null, false);

        beanForm = new BeanForm("beanForm", bean, meta);
        add(beanForm);
    }
    
    public void save(AjaxRequestTarget target, Form form, TestBean bean)
    {
        if (!beanForm.validateRequired()) {
            return; // Errors
        }
        
        info("Saved - thank you");
    }

    public void cancel(AjaxRequestTarget target, Form form, TestBean bean)
    {
        info("Canceled - thank you");
    }

    public void deleteRow(AjaxRequestTarget target, Form form, TestBean2 rowBean)
    {
        info("Deleted row " + rowBean);
        bean.getBeans().remove(rowBean);
    }

    public void addRow(AjaxRequestTarget target, Form form, TestBean rowBean)
    {
        bean.getBeans().add( new TestBean2() );
    }

    public void doIt(AjaxRequestTarget target, Form form, TestBean rowBean)
    {
        info("You did it");
    }
}