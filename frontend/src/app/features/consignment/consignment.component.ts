import { ChangeDetectionStrategy, Component, OnInit, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  ConsigneeResponse,
  ConsignmentContractResponse,
  ConsignmentHttpAdapter,
  ConsignorResponse,
  SentConsignmentContractResponse,
} from '../../infrastructure/http/consignment-http.adapter';

@Component({
  selector: 'app-consignment',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './consignment.component.html',
  styleUrl: './consignment.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ConsignmentComponent implements OnInit {
  readonly consignors = signal<ConsignorResponse[]>([]);
  readonly contracts = signal<ConsignmentContractResponse[]>([]);
  readonly consignees = signal<ConsigneeResponse[]>([]);
  readonly sentContracts = signal<SentConsignmentContractResponse[]>([]);
  readonly mode = signal<'received' | 'sent'>('received');
  readonly selectedContractUuid = signal<string | null>(null);
  readonly selectedSentContractUuid = signal<string | null>(null);
  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);

  readonly consignorForm = signal({ name: '', document: '', email: '', phone: '' });
  readonly contractForm = signal({ consignorUuid: '', code: '' });
  readonly receiveForm = signal({ varianteUuid: '', quantity: 1 });
  readonly returnForm = signal({ itemUuid: '', quantity: 1 });
  readonly settlementForm = signal({ itemUuid: '', quantity: 1, manualAmount: 0, notes: '' });
  readonly consigneeForm = signal({ name: '', document: '', email: '', phone: '' });
  readonly sentContractForm = signal({ consigneeUuid: '', code: '' });
  readonly sendForm = signal({ varianteUuid: '', quantity: 1 });
  readonly sentSaleForm = signal({ itemUuid: '', quantity: 1 });
  readonly sentReturnForm = signal({ itemUuid: '', quantity: 1 });
  readonly sentSettlementForm = signal({ itemUuid: '', quantity: 1, manualAmount: 0, notes: '' });

  readonly selectedContract = computed(() =>
    this.contracts().find((contract) => contract.uuid === this.selectedContractUuid()) ?? null,
  );

  readonly pendingItems = computed(() =>
    this.selectedContract()?.items.filter((item) => item.remainingQuantity > 0) ?? [],
  );

  readonly soldItems = computed(() =>
    this.selectedContract()?.items.filter((item) => item.soldQuantity > item.settledQuantity) ?? [],
  );

  readonly selectedSentContract = computed(() =>
    this.sentContracts().find((contract) => contract.uuid === this.selectedSentContractUuid()) ?? null,
  );

  readonly sentAvailableItems = computed(() =>
    this.selectedSentContract()?.items.filter((item) => item.availableQuantity > 0) ?? [],
  );

  readonly sentSoldItems = computed(() =>
    this.selectedSentContract()?.items.filter((item) => item.soldQuantity > item.settledQuantity) ?? [],
  );

  constructor(private readonly consignmentAdapter: ConsignmentHttpAdapter) {}

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.loading.set(true);
    this.errorMessage.set(null);
    this.consignmentAdapter.listConsignors().subscribe({
      next: (consignors) => {
        this.consignors.set(consignors);
        this.consignmentAdapter.listContracts().subscribe({
          next: (contracts) => {
            this.contracts.set(contracts);
            if (!this.selectedContractUuid() && contracts.length > 0) {
              this.selectedContractUuid.set(contracts[0].uuid);
            }
            this.loadSentData();
          },
          error: () => this.handleError('Não foi possível carregar contratos.'),
        });
      },
      error: () => this.handleError('Não foi possível carregar consignantes.'),
    });
  }

  loadSentData(): void {
    this.consignmentAdapter.listConsignees().subscribe({
      next: (consignees) => {
        this.consignees.set(consignees);
        this.consignmentAdapter.listSentContracts().subscribe({
          next: (contracts) => {
            this.sentContracts.set(contracts);
            if (!this.selectedSentContractUuid() && contracts.length > 0) {
              this.selectedSentContractUuid.set(contracts[0].uuid);
            }
            this.loading.set(false);
          },
          error: () => this.handleError('Não foi possível carregar contratos enviados.'),
        });
      },
      error: () => this.handleError('Não foi possível carregar consignatários.'),
    });
  }

  createConsignor(): void {
    const form = this.consignorForm();
    if (!form.name.trim()) return;
    this.loading.set(true);
    this.consignmentAdapter.createConsignor({
      name: form.name.trim(),
      document: form.document.trim() || null,
      email: form.email.trim() || null,
      phone: form.phone.trim() || null,
    }).subscribe({
      next: () => {
        this.consignorForm.set({ name: '', document: '', email: '', phone: '' });
        this.loadData();
      },
      error: () => this.handleError('Não foi possível criar consignante.'),
    });
  }

  openContract(): void {
    const form = this.contractForm();
    if (!form.consignorUuid || !form.code.trim()) return;
    this.loading.set(true);
    this.consignmentAdapter.openContract(form.consignorUuid, form.code.trim()).subscribe({
      next: (contract) => {
        this.selectedContractUuid.set(contract.uuid);
        this.contractForm.set({ consignorUuid: '', code: '' });
        this.loadData();
      },
      error: () => this.handleError('Não foi possível abrir contrato.'),
    });
  }

  receiveItem(): void {
    const contract = this.selectedContract();
    const form = this.receiveForm();
    if (!contract || !form.varianteUuid.trim() || form.quantity < 1) return;
    this.loading.set(true);
    this.consignmentAdapter.receiveItems(contract.uuid, [{
      varianteUuid: form.varianteUuid.trim(),
      quantity: Number(form.quantity),
    }]).subscribe({
      next: (updated) => this.applyUpdatedContract(updated, () => this.receiveForm.set({ varianteUuid: '', quantity: 1 })),
      error: () => this.handleError('Não foi possível receber item consignado.'),
    });
  }

  returnItem(): void {
    const contract = this.selectedContract();
    const form = this.returnForm();
    if (!contract || !form.itemUuid || form.quantity < 1) return;
    this.loading.set(true);
    this.consignmentAdapter.returnItems(contract.uuid, [{
      itemUuid: form.itemUuid,
      quantity: Number(form.quantity),
    }]).subscribe({
      next: (updated) => this.applyUpdatedContract(updated, () => this.returnForm.set({ itemUuid: '', quantity: 1 })),
      error: () => this.handleError('Não foi possível devolver item consignado.'),
    });
  }

  settleItem(): void {
    const contract = this.selectedContract();
    const form = this.settlementForm();
    if (!contract || !form.itemUuid || form.quantity < 1 || form.manualAmount <= 0) return;
    this.loading.set(true);
    this.consignmentAdapter.settle(contract.uuid, form.notes.trim() || null, [{
      itemUuid: form.itemUuid,
      quantity: Number(form.quantity),
      manualAmount: Number(form.manualAmount),
    }]).subscribe({
      next: () => {
        this.settlementForm.set({ itemUuid: '', quantity: 1, manualAmount: 0, notes: '' });
        this.loadData();
      },
      error: () => this.handleError('Não foi possível registrar acerto.'),
    });
  }

  closeContract(): void {
    const contract = this.selectedContract();
    if (!contract) return;
    this.loading.set(true);
    this.consignmentAdapter.closeContract(contract.uuid).subscribe({
      next: (updated) => this.applyUpdatedContract(updated),
      error: () => this.handleError('Contrato possui pendências ou não pode ser encerrado.'),
    });
  }

  createConsignee(): void {
    const form = this.consigneeForm();
    if (!form.name.trim()) return;
    this.loading.set(true);
    this.consignmentAdapter.createConsignee({
      name: form.name.trim(),
      document: form.document.trim() || null,
      email: form.email.trim() || null,
      phone: form.phone.trim() || null,
    }).subscribe({
      next: () => {
        this.consigneeForm.set({ name: '', document: '', email: '', phone: '' });
        this.loadData();
      },
      error: () => this.handleError('Não foi possível criar consignatário.'),
    });
  }

  openSentContract(): void {
    const form = this.sentContractForm();
    if (!form.consigneeUuid || !form.code.trim()) return;
    this.loading.set(true);
    this.consignmentAdapter.openSentContract(form.consigneeUuid, form.code.trim()).subscribe({
      next: (contract) => {
        this.selectedSentContractUuid.set(contract.uuid);
        this.sentContractForm.set({ consigneeUuid: '', code: '' });
        this.loadData();
      },
      error: () => this.handleError('Não foi possível abrir contrato enviado.'),
    });
  }

  sendItem(): void {
    const contract = this.selectedSentContract();
    const form = this.sendForm();
    if (!contract || !form.varianteUuid.trim() || form.quantity < 1) return;
    this.loading.set(true);
    this.consignmentAdapter.sendItems(contract.uuid, [{
      varianteUuid: form.varianteUuid.trim(),
      quantity: Number(form.quantity),
    }]).subscribe({
      next: (updated) => this.applyUpdatedSentContract(updated, () => this.sendForm.set({ varianteUuid: '', quantity: 1 })),
      error: () => this.handleError('Não foi possível enviar item consignado.'),
    });
  }

  reportSentSale(): void {
    const contract = this.selectedSentContract();
    const form = this.sentSaleForm();
    if (!contract || !form.itemUuid || form.quantity < 1) return;
    this.loading.set(true);
    this.consignmentAdapter.reportSentSales(contract.uuid, [{
      itemUuid: form.itemUuid,
      quantity: Number(form.quantity),
    }]).subscribe({
      next: (updated) => this.applyUpdatedSentContract(updated, () => this.sentSaleForm.set({ itemUuid: '', quantity: 1 })),
      error: () => this.handleError('Não foi possível registrar venda enviada.'),
    });
  }

  returnSentItem(): void {
    const contract = this.selectedSentContract();
    const form = this.sentReturnForm();
    if (!contract || !form.itemUuid || form.quantity < 1) return;
    this.loading.set(true);
    this.consignmentAdapter.returnSentItems(contract.uuid, [{
      itemUuid: form.itemUuid,
      quantity: Number(form.quantity),
    }]).subscribe({
      next: (updated) => this.applyUpdatedSentContract(updated, () => this.sentReturnForm.set({ itemUuid: '', quantity: 1 })),
      error: () => this.handleError('Não foi possível devolver item enviado.'),
    });
  }

  settleSentItem(): void {
    const contract = this.selectedSentContract();
    const form = this.sentSettlementForm();
    if (!contract || !form.itemUuid || form.quantity < 1 || form.manualAmount <= 0) return;
    this.loading.set(true);
    this.consignmentAdapter.settleSent(contract.uuid, form.notes.trim() || null, [{
      itemUuid: form.itemUuid,
      quantity: Number(form.quantity),
      manualAmount: Number(form.manualAmount),
    }]).subscribe({
      next: () => {
        this.sentSettlementForm.set({ itemUuid: '', quantity: 1, manualAmount: 0, notes: '' });
        this.loadData();
      },
      error: () => this.handleError('Não foi possível registrar acerto enviado.'),
    });
  }

  closeSentContract(): void {
    const contract = this.selectedSentContract();
    if (!contract) return;
    this.loading.set(true);
    this.consignmentAdapter.closeSentContract(contract.uuid).subscribe({
      next: (updated) => this.applyUpdatedSentContract(updated),
      error: () => this.handleError('Contrato enviado possui pendências ou não pode ser encerrado.'),
    });
  }

  setConsignorField(field: keyof ReturnType<typeof this.consignorForm>, value: string): void {
    this.consignorForm.set({ ...this.consignorForm(), [field]: value });
  }

  private applyUpdatedContract(updated: ConsignmentContractResponse, after?: () => void): void {
    this.contracts.set(this.contracts().map((contract) => contract.uuid === updated.uuid ? updated : contract));
    this.selectedContractUuid.set(updated.uuid);
    after?.();
    this.loading.set(false);
  }

  private applyUpdatedSentContract(updated: SentConsignmentContractResponse, after?: () => void): void {
    this.sentContracts.set(this.sentContracts().map((contract) => contract.uuid === updated.uuid ? updated : contract));
    this.selectedSentContractUuid.set(updated.uuid);
    after?.();
    this.loading.set(false);
  }

  private handleError(message: string): void {
    this.errorMessage.set(message);
    this.loading.set(false);
  }
}
