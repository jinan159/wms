package com.teamfresh.wms.infra.document;

import java.io.InputStream;

public interface SpreadSheetParser {
    SpreadSheetDocument parse(InputStream inputStream);
}
