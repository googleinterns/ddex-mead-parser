import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;

import java.util.Map;

public class ProtoMessageCleaner {
    public ProtoMessageCleaner() { }

    public void clean(DynamicMessage.Builder message) {
        Map<Descriptors.FieldDescriptor, Object> fields = message.getAllFields();
        System.out.println(fields);
    }
}
