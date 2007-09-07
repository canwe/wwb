
package wicket.contrib.webbeans.model;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * Common metadata methods. <p>
 * 
 * @author Dan Syrstad
 */
public class MetaData
{
    private Properties parameters = new Properties();
    private Set<String> consumedParameters = new HashSet<String>();

    /**
     * Construct a MetaData. 
     */
    public MetaData()
    {
    }

    /**
     * Consumes a parameter.
     *
     * @param param the parameter to consume.
     */
    public void consumeParameter(String param)
    {
        consumedParameters.add(param);
    }

    /**
     * @param unconsumedMsgs messages that report the parameter keys that were specified but not consumed.
     * @param context a context for the unconsumed message.
     * @return true if all parameters specified have been consumed.
     */
    public boolean areAllParametersConsumed(String context, Set<String> unconsumedMsgs)
    {
        boolean result = true;
        for (Object parameter : parameters.keySet()) {
            if (!consumedParameters.contains(parameter)) {
                unconsumedMsgs.add(context+ ": Parameter " + parameter + " was not consumed");
                result = false;
            }
        }
        
        return result;
    }

    /**
     * Gets the specified parameter.
     *
     * @param key the parameter key.
     * 
     * @return the parameter value, or null if not set.
     */
    public String getParameter(String key)
    {
        consumeParameter(key);
        return parameters.getProperty(key);
    }
    
    /**
     * Gets the specified parameter.
     *
     * @param key the parameter key.
     * 
     * @return the parameter value, or defaultValue if not set.
     */
    public String getParameter(String key, String defaultValue)
    {
        String value = getParameter(key);
        return value == null ? defaultValue : value;
    }
    
    /**
     * Gets a boolean parameter.
     *
     * @param key the parameter key.
     * 
     * @return true or false. False is returned if the parameter is not set.
     */
    public boolean getBooleanParameter(String key)
    {
        return Boolean.valueOf( getParameter(key, "false") );
    }
    
    /**
     * Gets an Integer parameter.
     *
     * @param key the parameter key.
     * 
     * @return the value, or null if not set.
     */
    public Integer getIntegerParameter(String key)
    {
        String value = getParameter(key);
        return value == null ? null : Integer.valueOf(value);
    }
    
    /**
     * Gets an int parameter.
     *
     * @param key the parameter key.
     * 
     * @return the value, or defaultValue if not set.
     */
    public int getIntParameter(String key, int defaultValue)
    {
        Integer value = getIntegerParameter(key);
        return value == null ? defaultValue : value;
    }
    
    /**
     * Sets a parameter.
     *
     * @param key the parameter key.
     * @param value the parameter value.
     */
    public void setParameter(String key, String value)
    {
        parameters.setProperty(key, value);
    }
    
    
    /**
     * Gets the parameters.
     *
     * @return the parameters.
     */
    public Properties getParameters()
    {
        return parameters;
    }
    
    /**
     * Sets the parameters.
     *
     * @param parameters the parameters to set.
     */
    public void setParameters(Properties parameters)
    {
        this.parameters = parameters;
    }

}
