import com.walletconnect.android.cacao.SignatureInterface;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class SignatureTest implements SignatureInterface {
    @NotNull
    private final String t;
    @NotNull
    private final String s;
    @org.jetbrains.annotations.Nullable
    private final String m;

    @NotNull
    public String getT() {
        return this.t;
    }

    @NotNull
    public String getS() {
        return this.s;
    }

    @org.jetbrains.annotations.Nullable
    public String getM() {
        return this.m;
    }

    public SignatureTest(@NotNull String t, @NotNull String s, @Nullable String m) {
        this.t = t;
        this.s = s;
        this.m = m;
    }
}