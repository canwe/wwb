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
import java.util.List;

/**
 * Meta data for a tab.
 */
public final class TabMetaData extends MetaData implements Serializable
{
    public static final String PARAM_LABEL = "label";
    
    private BeanMetaData beanMetaData;
    private String id;

    TabMetaData(BeanMetaData beanMetaData, String id, String label)
    {
        super(beanMetaData.getComponent());
        
        this.beanMetaData = beanMetaData;
        this.id = id;
        setLabel(label);
        
        consumeParameter(PARAM_LABEL);
    }

    /**
     * Applies parameters from a beanprops file to this tab.
     *
     * @param params
     */
    void applyBeanProps(List<ParameterAST> params)
    {
        for (ParameterAST param : params) {
            if (param.getName().equals(BeanMetaData.PARAM_PROPS)) {
                beanMetaData.applyProps(param.getValues(), id);
            }
            else {
                List<ParameterValueAST> values = param.getValues();
                if (values.size() != 1) {
                    throw new RuntimeException("Parameter " + param.getName() + "on tab " + id + " on bean " + beanMetaData.getBeanClass().getSimpleName() + " does not specify exactly one value.");
                }
                
                String value = values.get(0).getValue();
                setParameter(param.getName(), value);
            }
        }
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getLabel()
    {
        return getParameter(PARAM_LABEL);
    }

    public void setLabel(String label)
    {
        setParameter(PARAM_LABEL, label);
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

        final TabMetaData other = (TabMetaData) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        }

        return id.equals(other.id);
    }

    @Override
    public int hashCode()
    {
        int result = 1;
        result = 31 * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }
}