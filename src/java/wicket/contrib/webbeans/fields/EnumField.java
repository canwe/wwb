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

import wicket.contrib.webbeans.model.ElementMetaData;
import wicket.contrib.webbeans.model.NonJavaEnum;
import wicket.markup.html.form.DropDownChoice;
import wicket.markup.html.panel.Fragment;
import wicket.model.IModel;

import java.util.List;

/**
 * A field for enumerated types. Presents the values as a drop-down list.
 * Accepts a parameter of "default" indicating the default choice if the current selection is null.
 * 
 * @author Dan Syrstad
 */
abstract public class EnumField extends AbstractField
{
    /**
     * Construct a new EnumField.
     *
     * @param id the Wicket id for the editor.
     * @param model the model.
     * @param metaData the meta data for the property.
     * @param viewOnly true if the component should be view-only.
     * @param values a List of values to be selected from. The element's toString() is used to 
     *  produce the value displayed to the user.
     */
    public EnumField(String id, IModel model, ElementMetaData metaData, boolean viewOnly, List values)
    {
        super(id, model, metaData, viewOnly);

        Fragment fragment;
        if (viewOnly) {
            fragment = new Fragment("frag", "viewer");
            fragment.add(new LabelWithMinSize("component", model));
        }
        else {
            fragment = new Fragment("frag", "editor");
            DropDownChoice choice = new DropDownChoice("component", model, values);
            // Always allow the null choice.
            choice.setNullValid(true);
            fragment.add(choice);

            // Handle default selection if no model is provided
            if (choice.getModelObject() == null) {
                String defaultChoice = metaData.getParameter("default");
                if (defaultChoice != null && values.size() > 0)
                    setupDefault(values, defaultChoice, choice);
            }
        }

        add(fragment);
    }

    private void setupDefault(List values, String defaultChoice, DropDownChoice choice)
    {
        boolean isJavaEnum = false;
        Object firstValue = values.get(0);

        // Figure out what type of enum were dealing with
        if (firstValue instanceof Enum) {
            isJavaEnum = true;
        }
        else if (!(firstValue instanceof NonJavaEnum)) {
            throw new RuntimeException("Unexpected enum type. Enum must be a Java language Enum or a custom enumeration that implements NonJavaEnum");
        }

        // Iterate over the values and find the one that matches the default choice
        for (Object value : values) {
            if ((isJavaEnum && ((Enum)value).name().equals(defaultChoice)) ||
                (!isJavaEnum && ((NonJavaEnum)value).name().equals(defaultChoice)))  {
                choice.setModelObject(value);
                break;
            }
        }
    }
}
