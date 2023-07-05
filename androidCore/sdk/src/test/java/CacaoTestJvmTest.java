import com.walletconnect.android.cacao.signature.SignatureType;
import com.walletconnect.android.internal.common.model.ProjectId;
import com.walletconnect.android.internal.common.signing.cacao.Cacao;
import com.walletconnect.android.internal.common.signing.cacao.CacaoKt;
import com.walletconnect.android.internal.common.signing.cacao.CacaoType;
import com.walletconnect.android.internal.common.signing.cacao.CacaoVerifier;
import com.walletconnect.android.utils.cacao.CacaoSignerUtil;
import com.walletconnect.util.UtilFunctionsKt;

import org.junit.Assert;
import org.junit.Test;
import org.web3j.utils.Assertions;
import org.web3j.utils.Numeric;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

class CacaoTestJvmTest {
    ArrayList<String> resources = new ArrayList<>();
    byte[] privateKey = UtilFunctionsKt.hexToBytes("305c6cde3846927892cd32762f6120539f3ec74c9e3a16b9b798b1e85351ae2a");

    @Test
    public void jvmTestHexStringSigning() {
        CacaoVerifier cacaoVerifier = new CacaoVerifier(new ProjectId(""));

        resources.add("ipfs://bafybeiemxf5abjwjbikoz4mc3a3dla6ual3jsgpdr4cjr3oz3evfyavhwq/");
        resources.add("https://example.com/my-web2-claim.json");

        String iss = "did:pkh:eip155:1:0x15bca56b6e2728aec2532df9d436bd1600e86688";
        Cacao.Payload payload = new Cacao.Payload(
                iss,
                "service.invalid",
                "https://service.invalid/login",
                "1",
                "32891756",
                "2021-09-30T16:25:24Z",
                null,
                null,
                "I accept the ServiceOrg Terms of Service: https://service.invalid/tos",
                null,
                resources
        );

        String chainName = "Ethereum";
        String message = CacaoKt.toCAIP122Message(payload, chainName);
        SignatureTest signature = CacaoSignerUtil.signHex(SignatureTest.class, Numeric.toHexString(message.getBytes(StandardCharsets.UTF_8)), privateKey, SignatureType.EIP191);
        Cacao.Signature cacaoSig = new Cacao.Signature(signature.getT(), signature.getS(), signature.getM());
        Cacao cacao = new Cacao(CacaoType.EIP4361.toHeader(), payload, cacaoSig);

        boolean result = cacaoVerifier.verify(cacao);
        Assert.assertTrue(result);
    }

    @Test
    public void jvmTestPlainStringSigning() {
        CacaoVerifier cacaoVerifier = new CacaoVerifier(new ProjectId(""));

        resources.add("ipfs://bafybeiemxf5abjwjbikoz4mc3a3dla6ual3jsgpdr4cjr3oz3evfyavhwq/");
        resources.add("https://example.com/my-web2-claim.json");

        String iss = "did:pkh:eip155:1:0x15bca56b6e2728aec2532df9d436bd1600e86688";
        Cacao.Payload payload = new Cacao.Payload(
                iss,
                "service.invalid",
                "https://service.invalid/login",
                "1",
                "32891756",
                "2021-09-30T16:25:24Z",
                null,
                null,
                "I accept the ServiceOrg Terms of Service: https://service.invalid/tos",
                null,
                resources
        );

        String chainName = "Ethereum";
        String message = CacaoKt.toCAIP122Message(payload, chainName);
        SignatureTest signature = CacaoSignerUtil.sign(SignatureTest.class, message, privateKey, SignatureType.EIP191);
        Cacao.Signature cacaoSig = new Cacao.Signature(signature.getT(), signature.getS(), signature.getM());
        Cacao cacao = new Cacao(CacaoType.EIP4361.toHeader(), payload, cacaoSig);

        boolean result = cacaoVerifier.verify(cacao);
        Assert.assertTrue(result);
    }
}