package org.autopipes.takeout;

import org.autopipes.util.OxmResourceReader;
import org.springframework.core.io.Resource;

public class TakeoutResourceReader extends OxmResourceReader<TakeoutRepository> {
	TakeoutRepository read(final Resource resource) throws Exception {
		return read(resource, TakeoutRepository.class );
	}
}
