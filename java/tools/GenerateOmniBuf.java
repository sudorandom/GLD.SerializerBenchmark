import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse;
import com.omnibuf.plugin.ProtocGenOmniBufJava;

import java.nio.file.Files;
import java.nio.file.Path;

public final class GenerateOmniBuf {
  public static void main(String[] args) throws Exception {
    Path descPath = Path.of(args[0]);
    Path outDir = Path.of(args[1]);
    FileDescriptorSet fds = FileDescriptorSet.parseFrom(Files.readAllBytes(descPath));
    CodeGeneratorRequest.Builder req = CodeGeneratorRequest.newBuilder();
    for (FileDescriptorProto f : fds.getFileList()) {
      req.addFileToGenerate(f.getName());
      req.addProtoFile(
          f.toBuilder()
              .setOptions(f.getOptions().toBuilder().setJavaPackage("benchmark.v2.omnibuf"))
              .build());
    }
    CodeGeneratorResponse resp = ProtocGenOmniBufJava.generate(req.build());
    if (!resp.getError().isEmpty()) {
      throw new IllegalStateException("protoc-gen-omnibuf-java: " + resp.getError());
    }
    for (CodeGeneratorResponse.File file : resp.getFileList()) {
      Path dst = outDir.resolve(file.getName());
      Files.createDirectories(dst.getParent());
      Files.writeString(dst, file.getContent());
    }
  }
}
