package io.gumga.presentation;

import java.io.Serializable;

@Deprecated
public abstract class GumgaDTOAPI<T, ID extends Serializable> extends AbstractGumgaAPI<T, ID> {

	public GumgaDTOAPI(GumgaGateway<?, ID, T> gateway) {
		super(gateway);
	}
	
}
