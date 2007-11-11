/*---
   Copyright 2007 Visual Systems Corporation.
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

package wicket.contrib.webbeans.model.api;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import wicket.contrib.webbeans.annotations.Action;
import wicket.contrib.webbeans.annotations.Parameter;

/**
 * Bean Action API implementation. Provides information from the {@link Action} annotation in a Java API form. <p>
 * 
 * @author Dan Syrstad
 */
public class JAction implements Action, Serializable
{
    private String name = "";
    private boolean isDefault = false;
    private String confirm = "";
    private boolean ajax = false;

    // --- Common to Action and Property
    private String label =  "";
    private String labelImage = "";
    private int colspan = 1;
    private List<Parameter> params = new ArrayList<Parameter>();
    private String paramName = "";
    private String paramValue = "";

    /**
     * Construct a JAction. 
     */
    public JAction()
    {
    }

    /**
     * Construct a JAction. 
     */
    public JAction(String name)
    {
        this.name = name;
    }

    /** 
     * {@inheritDoc}
     * @see wicket.contrib.webbeans.annotations.Action#colspan()
     */
    public int colspan()
    {
        return this.colspan;
    }
    
    public JAction colspan(int colspan)
    {
        this.colspan = colspan;
        return this;
    }

    /** 
     * {@inheritDoc}
     * @see wicket.contrib.webbeans.annotations.Action#label()
     */
    public String label()
    {
        return label;
    }
    
    public JAction label(String label)
    {
        this.label = label;
        return this;
    }
    
    /** 
     * {@inheritDoc}
     * @see wicket.contrib.webbeans.annotations.Action#labelImage()
     */
    public String labelImage()
    {
        return labelImage;
    }
    
    /** @see #labelImage() */
    public JAction labelImage(String labelImage)
    {
        this.labelImage = labelImage;
        return this;
    }

    /** 
     * {@inheritDoc}
     * @see wicket.contrib.webbeans.annotations.Action#name()
     */
    public String name()
    {
        return name;
    }
    
    /** @see #name() */
    public JAction name(String name)
    {
        this.name = name;
        return this;
    }

    /** 
     * {@inheritDoc}
     * @see wicket.contrib.webbeans.annotations.Action#paramName()
     */
    public String paramName()
    {
        return paramName;
    }
    
    /** 
     * {@inheritDoc}
     * @see wicket.contrib.webbeans.annotations.Action#paramValue()
     */
    public String paramValue()
    {
        return paramValue;
    }

    /** 
     * Shortcut for {@link #paramName()} and {@link #paramValue()}.
     */
    public JAction param(String name, String value)
    {
        paramName = name;
        paramValue = value;
        return this;
    }

    /** 
     * {@inheritDoc}
     * @see wicket.contrib.webbeans.annotations.Action#params()
     */
    public Parameter[] params()
    {
        return params.toArray(new Parameter[ params.size() ]);
    }
    
    /** @see #params() */
    public JAction params(Parameter... params)
    {
        this.params = new ArrayList<Parameter>( Arrays.asList(params) ); // Must be mutable
        return this;
    }
    
    /** @see #params() */
    public JAction params(List<Parameter> params)
    {
        this.params = params;
        return this;
    }
    
    /** @see #params() */
    public JAction add(Parameter param)
    {
        this.params.add(param);
        return this;
    }

    /** 
     * Shortcut for {@link #paramName()} and {@link #paramValue()}. 
     */
    public JAction add(String name, String... values)
    {
        params.add( new JParameter(name, values) );
        return this;
    }

    /** 
     * {@inheritDoc}
     * @see wicket.contrib.webbeans.annotations.Action#ajax()
     */
    public boolean ajax()
    {
        return ajax;
    }
    
    /** @see #ajax() */
    public JAction ajax(boolean ajax)
    {
        this.ajax = ajax;
        return this;
    }

    /** 
     * {@inheritDoc}
     * @see wicket.contrib.webbeans.annotations.Action#confirm()
     */
    public String confirm()
    {
        return confirm;
    }
    
    /** @see #confirm() */
    public JAction confirm(String confirm)
    {
        this.confirm = confirm;
        return this;
    }

    /** 
     * {@inheritDoc}
     * @see wicket.contrib.webbeans.annotations.Action#isDefault()
     */
    public boolean isDefault()
    {
        return isDefault;
    }
    
    /** @see #isDefault() */
    public JAction isDefault(boolean isDefault)
    {
        this.isDefault = isDefault;
        return this;
    }

    /** 
     * {@inheritDoc}
     * @see java.lang.annotation.Annotation#annotationType()
     */
    public Class<? extends Annotation> annotationType()
    {
        return JAction.class;
    }

}
