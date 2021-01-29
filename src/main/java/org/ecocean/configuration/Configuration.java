package org.ecocean.configuration;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.io.File;
import org.ecocean.Util;
import org.ecocean.DataDefinition;
import org.ecocean.DataDefinitionException;
import org.ecocean.Shepherd;
import org.json.JSONObject;
import org.json.JSONArray;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class Configuration implements java.io.Serializable {
    private String id;
    private String content = null;
    private long created;
    private long modified;

    public Configuration() {
        this.created = System.currentTimeMillis();
        this.setModified();
    }
    public Configuration(String id) {
        this();
        this.id = id;
    }
    public Configuration(String id, JSONObject cont) {
        this();
        if (cont != null) this.content = cont.toString();
        this.id = id;
    }

    public String getId() {
        return id;
    }
    public String getParentId() {
        return ConfigurationUtil.parentId(id);
    }

    public JSONObject getContent() {
        return Util.stringToJSONObject(content);
    }

    //do NOT setContent directly, instead use ConfigurationUtil.setConfigurationValue()
    public void setContent(JSONObject j) {
        if (j == null) {
            content = null;
        } else {
            content = j.toString();
        }
        this.setModified();
    }

    public void setValue(Shepherd myShepherd, Object value) throws ConfigurationException, DataDefinitionException {  //but, convenience
        ConfigurationUtil.setConfigurationValue(myShepherd, this.id, value);
    }

    public boolean hasValue() {
        JSONObject c = this.getContent();
        if ((c == null) || !c.has(ConfigurationUtil.VALUE_KEY)) return false;
        return true;
    }

    //we have various flavors.  note the *List ones can be used to grab non-multiple values but as a list
    // we do our best to cast values even if type does not match
    public String getValueAsString() throws DataDefinitionException {
        return DataDefinition.coerceString(this.getContent(), this._precheckSingle());
    }
    public Integer getValueAsInteger() throws DataDefinitionException {
        return DataDefinition.coerceInteger(this.getContent(), this._precheckSingle());
    }
    public Double getValueAsDouble() throws DataDefinitionException {
        return DataDefinition.coerceDouble(this.getContent(), this._precheckSingle());
    }
    public Boolean getValueAsBoolean() throws DataDefinitionException {
        return DataDefinition.coerceBoolean(this.getContent(), this._precheckSingle());
    }
    public JSONObject getValueAsJSONObject() throws DataDefinitionException {
        return DataDefinition.coerceJSONObject(this.getContent(), this._precheckSingle());
    }
    public JSONArray getValueAsJSONArray() throws DataDefinitionException {
        return DataDefinition.coerceJSONArray(this.getContent(), this._precheckMultiple());
    }

    public List<String> getValueAsStringList() throws DataDefinitionException {
/*
        JSONObject meta = this._precheckMultiple();
        if (!this.isMultiple(meta)) {
            List<String> rtn = new ArrayList<String>();
            rtn.add(ConfigurationUtil.coerceString(this.getContent(), meta));
            return rtn;
        }
*/
        return DataDefinition.coerceStringList(this.getContent(), this._precheckMultiple());
    }

    private JSONObject _precheckMultiple() throws DataDefinitionException {
        if (!this.hasValidRoot()) throw new DataDefinitionException("invalid root on id=" + this.id);
        JSONObject meta = this.getMeta();
        if (meta == null) throw new DataDefinitionException("missing meta on id=" + this.id);
        return meta;
    }
    private JSONObject _precheckSingle() throws DataDefinitionException {
        if (!this.hasValidRoot()) throw new DataDefinitionException("invalid root on id=" + this.id);
        JSONObject meta = this.getMeta();
        if (meta == null) throw new DataDefinitionException("missing meta on id=" + this.id);
        if (this.isMultiple()) throw new DataDefinitionException("calling single value on multiple for id=" + this.id);
        return meta;
    }

/*
    public Object getValue() throws ConfigurationException {  //convenience
        return ConfigurationUtil.coerceValue(this);
    }

    public Object getDefaultValue() {
        if (!this.hasValidRoot()) return null;
        JSONObject meta = this.getMeta();
        String type = ConfigurationUtil.getType(meta);
return null; ///FIXME
        //return _coerce(meta, "defaultValue", type);
    }
*/

    public List<String> getIdPath() {
        return ConfigurationUtil.idPath(this.id);
    }
    public boolean hasValidRoot() {
        return ConfigurationUtil.idHasValidRoot(this.id);
    }
    public String getRoot() {
        return ConfigurationUtil.idGetRoot(this.id);
    }

    //the only kind that should be persisted to db!!!
    public boolean isRootLevel() {
        return ConfigurationUtil.isValidRoot(this.id);
    }

    public void resetRootCache() {
        ConfigurationUtil.resetValueCache(this.getRoot());
    }

    //this means it "can" or "should" have a value (e.g. path is good, meta defined etc)
    //  note that this is false you cannot read or set value on it
    public boolean isValid() {
        return isValid(this.getMeta());
    }
    public boolean isValid(JSONObject meta) {
        if (meta == null) return false;
        if (!this.hasValidRoot()) return false;
        if (ConfigurationUtil.getType(meta) == null) return false;
        return true;
    }
    public boolean isReadOnly() {
        return isReadOnly(this.getMeta());
    }
    public boolean isReadOnly(JSONObject meta) {
        if (!this.isValid(meta)) return true;  //kinda wonky; but also true!
        return DataDefinition.isReadOnly(meta);
    }

    //covers both the VALUE and the META (e.g. to public)
    public boolean isPrivate() {
        return isPrivate(this.getMeta());
    }
    public boolean isPrivate(JSONObject meta) {
        if (!this.isValid(meta)) return true;  //kinda wonky; but also true!
        return DataDefinition.isPrivate(meta);
    }

    public JSONObject getMeta() {
        return ConfigurationUtil.getMeta(this.id);
    }
    public JSONObject getNode() {
        return ConfigurationUtil.getNode(this.id);
    }
    public String getType(JSONObject meta) {
        return ConfigurationUtil.getType(meta);
    }
    public String getType() {
        return ConfigurationUtil.getType(this.id);
    }
    public Set<String> getChildKeys() {
        Set<String> ck = new HashSet<String>();
        JSONObject node = this.getNode();
        if ("".equals(this.id)) {  //special case cuz we just wanna hack the root ids as kids!
            for (String k : ConfigurationUtil.getMeta().keySet()) {
                ck.add(k);
            }

        } else if (node == null) {
            return ck;
        } else {
            for (Object k : node.keySet()) {
                String ks = (String)k;
                if (!ks.startsWith("_")) ck.add(ks);
            }
        }
        return ck;
    }
    public boolean hasChildren() {
        return (this.getChildKeys().size() > 0);
    }

    public void setModified() {
        modified = System.currentTimeMillis();
    }
    public long getModified() {
        return modified;
    }
    public long getCreated() {
        return created;
    }

    //mostly for front end
    public String getKey() {
        return ConfigurationUtil.idToKey(this.id);
    }
    public String getLang() {
        return ConfigurationUtil.idToLang(this.id);
    }
    public boolean isMultiple() {
        return DataDefinition.isMultiple(this.getMeta());
    }

    // based on https://github.com/WildbookOrg/wildbook-frontend/blob/master/src/constants/userSchema.js
    public JSONObject toFrontEndJSONObject() {
        return toFrontEndJSONObject(null);
    }
    public JSONObject toFrontEndJSONObject(Shepherd myShepherd) {
        JSONObject m = this.getMeta();
        JSONObject j = new JSONObject();
        j.put("configurationId", id);
        j.put("parentConfigurationId", this.getParentId());
        j.put("name", this.getKey());
        j.put("translationId", this.getLang());
        //then some explicit ones for ben:
        j.put("labelId", this.getLang() + "_LABEL");
        j.put("descriptionId", this.getLang() + "_DESCRIPTION");
        Set<String> kids = this.getChildKeys();
        if (!Util.collectionIsEmptyOrNull(kids)) j.put("childrenKeys", kids);
        String overrideDisplayType = null;
        if (m != null) {
            JSONObject fs = m.optJSONObject("formSchema");
            if (fs != null) {
                overrideDisplayType = fs.optString("displayType", null);
                j.put("schema", fs);
            }
            j.put("suggestedValues", m.optJSONArray("suggestedValues"));  //primarily for site.species (taxonomy hints) now
        }
        if (!this.isValid(m)) {
            j.put("settable", false);
            return j;  //no need to continue
        }

        String type = ConfigurationUtil.getType(m);
        j.put("displayType", type);  //for ben
        //now customize for front-end
        if (type.equals("video")) {
            j.put("displayType", "file");
            j.put("allowedFileTypes", new JSONArray("[\".webm\", \".mp4\", \".mov\", \".avi\"]"));
        } else if (type.equals("image")) {
            j.put("displayType", "file");
            j.put("allowedFileTypes", new JSONArray("[\".jpg\", \".jpeg\", \".png\", \".svg\"]"));
        } else if (type.equals("url")) {
            j.put("displayType", "string");
        }
        if (overrideDisplayType != null) j.put("displayType", overrideDisplayType);

        JSONObject c = this.getContent();
        if (c != null) j.put("currentValue", c.opt(ConfigurationUtil.VALUE_KEY));  //FIXME probably
        j.put("settable", m.optBoolean("settable", true));
        j.put("isPrivate", this.isPrivate(m));
        j.put("defaultValue", m.opt("defaultValue"));
        j.put("fieldType", type);
        j.put("required", m.optBoolean("required", false));
        j.put("readOnly", m.optBoolean("readOnly", false));
        int min = m.optInt("multipleMin", -1);
        int max = m.optInt("multipleMax", -1);
        if (min > -1) {
            j.put("multiple", true);
            j.put("multipleMin", min);
            if (min > 0) j.put("required", true);
        }
        if ((max > -1) && (max >= min)) {
            j.put("multiple", true);
            j.put("multipleMax", max);
        }
        if (this.isMultiple()) j.put("multiple", true);
        if (m.optJSONArray("values") != null) j.put("values", m.getJSONArray("values"));
        JSONObject vobj = m.optJSONObject("values");
        if (vobj != null) {  //got something complex...
            String sql = vobj.optString("sql", null);
            if (sql != null) {
                //TODO should we cache this?  or let it stay fresh/synced with db?
                if (myShepherd == null) {
                    //  ... maybe only use cache when no shepherd?
                    System.out.println("WARNING: .toFrontEndJSONObject() called without myShepherd but sql lookup needed for " + this);
                    j.put("_valuesError", "sql lookup but no Shepherd");
                } else {
                    try {
                        List<Object> list = ConfigurationUtil.sqlLookup(myShepherd, sql);
                        JSONArray vlist = new JSONArray();
                        for (Object o : list) {
                            if ("string".equals(type)) {
                                vlist.put((String)o);
                            } else if ("integer".equals(type)) {
                                vlist.put((Integer)o);
                            } else if ("double".equals(type)) {
                                vlist.put((Double)o);
                            } else if ("long".equals(type)) {
                                vlist.put((Long)o);
                            } else {
                                vlist.put(o);
                            }
                        }
                        j.put("values", vlist);
                    } catch (Exception ex) {
                        System.out.println("WARNING: .toFrontEndJSONObject() sql lookup on " + this + " threw " + ex.toString());
                        j.put("_valuesError", "sql lookup failed");
                    }
                }
            }
        }
        //we call this now cuz .values should be populated (e.g. lookup)
        JSONObject vlabels = ConfigurationUtil.frontEndValueLabels(m, j.optJSONArray("values"), this.id);
        if (vlabels != null) j.put("valueLabels", vlabels);
        return j;
    }

    public JSONObject toJSONObject() {
        JSONObject m = this.getMeta();
        JSONObject j = new JSONObject();
        j.put("id", id);
        j.put("idPath", new JSONArray(this.getIdPath()));
        j.put("meta", m);
        j.put("type", this.getType(m));
        j.put("isRootLevel", this.isRootLevel());
        j.put("isValid", this.isValid(m));
        j.put("readOnly", this.isReadOnly(m));
        j.put("hasValue", this.hasValue());
        j.put("isMultiple", this.isMultiple());
        j.put("isPrivate", this.isPrivate());
        j.put("validRoot", this.hasValidRoot());
        j.put("content", this.getContent());
/*
        try {
            j.put("value", this.getValueStringList());
        } catch (ConfigurationException ex) {}
*/
        j.put("created", this.getCreated());
        j.put("modified", this.getModified());
        return j;
    }

    public String toString() {
        JSONObject m = this.getMeta();
        return new ToStringBuilder(this)
                .append("id", id)
                .append("type", this.getType(m))
                .append("readOnly", this.isReadOnly(m))
                .append("isRootLevel", this.isRootLevel())
                .append("isValid", this.isValid(m))
                .append("hasValue", this.hasValue())
                .append("validRoot", this.hasValidRoot())
                .toString();
    }

    //i guess we should have this!  based on sharing same id, sorrynotsorry?
    public boolean equals(final Object c2) {
        if (c2 == null) return false;
        if (!(c2 instanceof Configuration)) return false;
        Configuration two = (Configuration)c2;
        if ((this.id == null) || (two == null) || (two.getId() == null)) return false;
        return this.id.equals(two.getId());
    }
}
