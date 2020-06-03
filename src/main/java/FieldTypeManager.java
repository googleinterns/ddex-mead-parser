import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FieldTypeManager {
    Map<String, List<QName>> fieldToTypesList;    /* Could replace value with new DataType */
    Map<QName, String> typeToFlagMap;

    public FieldTypeManager() {
        fieldToTypesList = new HashMap<>();
        typeToFlagMap = new HashMap<>();
    }

    // Should probably change to QName input
    public void addFieldToType(String field, QName type) {
        if (!fieldToTypesList.containsKey(field)) {
            fieldToTypesList.put(field, new ArrayList<>());
        }
        fieldToTypesList.get(field).add(type);
    }

    public List<QName> getTypesOfField(String name) {
        return fieldToTypesList.get(name);
    }
    public boolean isUnambiguousField(String name) {
        return fieldToTypesList != null && fieldToTypesList.get(name).size() == 1;
    }
}
