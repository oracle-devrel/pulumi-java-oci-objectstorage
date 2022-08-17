package myproject;

import com.pulumi.Pulumi;
import com.pulumi.core.Output;
import com.pulumi.oci.Identity.Compartment;
import com.pulumi.oci.Identity.CompartmentArgs;
import com.pulumi.oci.ObjectStorage.Bucket;
import com.pulumi.oci.ObjectStorage.BucketArgs;
import com.pulumi.oci.ObjectStorage.ObjectStorageFunctions;
import com.pulumi.oci.ObjectStorage.inputs.GetNamespaceArgs;
import com.pulumi.oci.ObjectStorage.outputs.GetNamespaceResult;

import java.util.concurrent.CompletableFuture;


public class App {
    public static void main(String[] args) {
        Pulumi.run(ctx -> {
            var config = ctx.config();
            Compartment myCompartment = new Compartment(config.require("compartment_name"),
                    CompartmentArgs.builder()
                            .name(config.require("compartment_name"))
                            .enableDelete(true)
                            .description(config.require("compartment_description")).build()
            );
            ctx.export("Compartment name", myCompartment.name());

            Output<String> namespace = Output.all(myCompartment.getId()).apply(values -> {
                try {
                    CompletableFuture<GetNamespaceResult> result = ObjectStorageFunctions.getNamespace(GetNamespaceArgs.builder()
                            .compartmentId(values.get(0))
                            .build());

                    return Output.of(result.get().namespace());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            Bucket myBucket = new Bucket(config.require("objectstorage_bucket_name"),
                    BucketArgs.builder()
                            .name(config.require("objectstorage_bucket_name"))
                            .compartmentId(myCompartment.getId())
                            .namespace(namespace)
                            .build());

            ctx.export("Bucket name", myBucket.name());

        });
    }
}