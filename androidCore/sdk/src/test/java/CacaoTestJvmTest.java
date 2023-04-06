import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.walletconnect.android.cacao.CacaoSignerInterfaceKt;
import com.walletconnect.android.cacao.SignatureInterface;
import com.walletconnect.android.cacao.signature.SignatureType;
import com.walletconnect.android.internal.common.cacao.Cacao;
import com.walletconnect.android.internal.common.cacao.CacaoKt;
import com.walletconnect.android.internal.common.cacao.CacaoType;
import com.walletconnect.android.internal.common.cacao.CacaoVerifier;
import com.walletconnect.android.internal.common.model.ProjectId;
import com.walletconnect.util.UtilFunctionsKt;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

class CacaoTestJvmTest {
    ArrayList<String> resources = new ArrayList<>();
    byte[] privateKey = UtilFunctionsKt.hexToBytes("305c6cde3846927892cd32762f6120539f3ec74c9e3a16b9b798b1e85351ae2a");

    @Test
    public void jvmTestSignature() {
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
        Cacao.Signature signature = (Cacao.Signature) CacaoSignerInterfaceKt.signCacao(Cacao.Signature.class, message, privateKey, SignatureType.EIP191);
        Cacao cacao = new Cacao(CacaoType.EIP4361.toHeader(), payload, signature);

        boolean result = cacaoVerifier.verify(cacao);
        Assertions.assertTrue(result);
    }
}