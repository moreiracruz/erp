package com.erp.modules.sales.adapter.in.web;

import java.util.UUID;

public record OpenSaleRequest(String terminalId, UUID clienteUuid) {}
