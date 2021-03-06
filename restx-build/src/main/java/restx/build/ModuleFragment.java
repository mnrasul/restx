package restx.build;

import java.io.IOException;
import java.io.Writer;

/**
 * User: xavierhanin
 * Date: 4/15/13
 * Time: 12:32 PM
 */
public class ModuleFragment {
    private final String fragment;

    public ModuleFragment(String fragment) {
        this.fragment = fragment;
    }

    public void write(ModuleDescriptor md, Writer w) throws IOException {
        w.write(fragment);
    }
}
