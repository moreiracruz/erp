import { describe, it, expect, beforeEach } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import * as fc from 'fast-check';
import { AddressesComponent } from './addresses.component';

const REQUIRED_FIELDS = ['street', 'number', 'neighborhood', 'city', 'state', 'cep'] as const;

/**
 * Property 4: Address CRUD preserves collection invariants
 * Validates: Requirements 3.3
 */
describe('AddressesComponent - Property 4: Address CRUD preserves collection invariants', () => {
  let component: AddressesComponent;

  const stateArb = fc.constantFrom('SP', 'RJ', 'MG', 'BA', 'RS', 'PR', 'SC', 'CE', 'PE', 'GO');

  const addressArb = fc.record({
    id: fc.uuid(),
    street: fc.string({ minLength: 1, maxLength: 50 }),
    number: fc.string({ minLength: 1, maxLength: 10 }),
    complement: fc.string({ maxLength: 30 }),
    neighborhood: fc.string({ minLength: 1, maxLength: 30 }),
    city: fc.string({ minLength: 1, maxLength: 30 }),
    state: stateArb,
    cep: fc.string({ minLength: 9, maxLength: 9 }),
  });

  const addressListArb = fc.array(addressArb, { minLength: 0, maxLength: 10 });

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddressesComponent, ReactiveFormsModule],
    }).compileComponents();

    const fixture = TestBed.createComponent(AddressesComponent);
    component = fixture.componentInstance;
  });

  it('Add: adding an address to a list of N produces N+1 addresses containing the new entry', () => {
    fc.assert(
      fc.property(addressListArb, addressArb, (initialList, newAddressData) => {
        // Set up initial state
        component.addresses.set(initialList);
        const n = initialList.length;

        // Simulate adding: open form, fill data, save
        component.openForm();
        component.form.patchValue({
          street: newAddressData.street,
          number: newAddressData.number,
          complement: newAddressData.complement,
          neighborhood: newAddressData.neighborhood,
          city: newAddressData.city,
          state: newAddressData.state,
          cep: newAddressData.cep,
        });

        component.onSave();

        const result = component.addresses();

        // List length increased by 1
        expect(result.length).toBe(n + 1);

        // The new entry is contained in the list (match by field values)
        const added = result.find(
          a =>
            a.street === newAddressData.street &&
            a.number === newAddressData.number &&
            a.complement === newAddressData.complement &&
            a.neighborhood === newAddressData.neighborhood &&
            a.city === newAddressData.city &&
            a.state === newAddressData.state &&
            a.cep === newAddressData.cep,
        );
        expect(added).toBeDefined();
      }),
      { numRuns: 100 },
    );
  });

  it('Delete: deleting an existing address ID from a non-empty list produces N-1 and ID is no longer present', () => {
    fc.assert(
      fc.property(
        addressListArb.filter(list => list.length > 0),
        fc.nat(),
        (initialList, indexSeed) => {
          // Ensure unique IDs in the initial list
          const uniqueList = initialList.map((a, i) => ({ ...a, id: `addr-${i}` }));
          component.addresses.set(uniqueList);

          const n = uniqueList.length;
          const targetIndex = indexSeed % n;
          const targetId = uniqueList[targetIndex].id;

          component.deleteAddress(targetId);

          const result = component.addresses();

          // List length decreased by 1
          expect(result.length).toBe(n - 1);

          // The deleted ID is no longer present
          const found = result.find(a => a.id === targetId);
          expect(found).toBeUndefined();
        },
      ),
      { numRuns: 100 },
    );
  });

  it('Edit: editing an existing address preserves list length and updates the targeted entry', () => {
    fc.assert(
      fc.property(
        addressListArb.filter(list => list.length > 0),
        addressArb,
        fc.nat(),
        (initialList, newFieldValues, indexSeed) => {
          // Ensure unique IDs in the initial list
          const uniqueList = initialList.map((a, i) => ({ ...a, id: `addr-${i}` }));
          component.addresses.set(uniqueList);

          const n = uniqueList.length;
          const targetIndex = indexSeed % n;
          const targetAddress = uniqueList[targetIndex];

          // Simulate editing: editAddress, change fields, save
          component.editAddress(targetAddress);
          component.form.patchValue({
            street: newFieldValues.street,
            number: newFieldValues.number,
            complement: newFieldValues.complement,
            neighborhood: newFieldValues.neighborhood,
            city: newFieldValues.city,
            state: newFieldValues.state,
            cep: newFieldValues.cep,
          });

          component.onSave();

          const result = component.addresses();

          // List length remains unchanged
          expect(result.length).toBe(n);

          // The targeted entry has updated fields
          const edited = result.find(a => a.id === targetAddress.id);
          expect(edited).toBeDefined();
          expect(edited!.street).toBe(newFieldValues.street);
          expect(edited!.number).toBe(newFieldValues.number);
          expect(edited!.complement).toBe(newFieldValues.complement);
          expect(edited!.neighborhood).toBe(newFieldValues.neighborhood);
          expect(edited!.city).toBe(newFieldValues.city);
          expect(edited!.state).toBe(newFieldValues.state);
          expect(edited!.cep).toBe(newFieldValues.cep);
        },
      ),
      { numRuns: 100 },
    );
  });
});


/**
 * Property 5: Address form validation rejects incomplete data
 * Validates: Requirements 3.4
 */
describe('AddressesComponent - Property 5: Address form validation rejects incomplete data', () => {
  let component: AddressesComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddressesComponent, ReactiveFormsModule],
    }).compileComponents();

    const fixture = TestBed.createComponent(AddressesComponent);
    component = fixture.componentInstance;
  });

  /**
   * Arbitrary that generates address field objects where at least one required field is empty.
   * Strategy: generate full valid values for all fields, then blank out at least one required field.
   */
  const incompleteAddressArb = fc
    .record({
      street: fc.string({ minLength: 0, maxLength: 50 }),
      number: fc.string({ minLength: 0, maxLength: 10 }),
      complement: fc.string({ maxLength: 30 }),
      neighborhood: fc.string({ minLength: 0, maxLength: 30 }),
      city: fc.string({ minLength: 0, maxLength: 30 }),
      state: fc.string({ minLength: 0, maxLength: 2 }),
      cep: fc.string({ minLength: 0, maxLength: 9 }),
      // Which required fields to blank (at least one)
      blankedFields: fc
        .subarray([...REQUIRED_FIELDS], { minLength: 1 })
    })
    .map(({ blankedFields, ...fields }) => {
      // Ensure non-blanked required fields have at least 1 character
      const result: Record<string, string> = { ...fields };
      for (const field of REQUIRED_FIELDS) {
        if (blankedFields.includes(field)) {
          result[field] = '';
        } else if (result[field].length === 0) {
          result[field] = 'x'; // ensure non-blank for fields not being tested
        }
      }
      return result;
    });

  it('form is invalid when at least one required field is empty', () => {
    fc.assert(
      fc.property(incompleteAddressArb, (addressFields) => {
        component.form.patchValue(addressFields);
        component.form.updateValueAndValidity();

        expect(component.form.invalid).toBe(true);
      }),
      { numRuns: 100 },
    );
  });
});


/**
 * Unit Tests: AddressesComponent
 * Validates: Requirements 3.1, 3.3
 */
describe('AddressesComponent - Unit Tests', () => {
  let component: AddressesComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddressesComponent, ReactiveFormsModule],
    }).compileComponents();

    const fixture = TestBed.createComponent(AddressesComponent);
    component = fixture.componentInstance;
  });

  describe('openForm()', () => {
    it('sets showForm to true and resets editingId to null', () => {
      // Set some prior state to verify reset behavior
      component.editingId.set('some-id');
      component.showForm.set(false);

      component.openForm();

      expect(component.showForm()).toBe(true);
      expect(component.editingId()).toBeNull();
    });
  });

  describe('editAddress()', () => {
    it('populates the form with address data and sets editingId', () => {
      const address = {
        id: 'addr-1',
        street: 'Rua Teste',
        number: '42',
        complement: 'Sala 3',
        neighborhood: 'Centro',
        city: 'Curitiba',
        state: 'PR',
        cep: '80000-000',
      };

      component.editAddress(address);

      expect(component.editingId()).toBe('addr-1');
      expect(component.showForm()).toBe(true);
      expect(component.form.value).toEqual(expect.objectContaining({
        street: 'Rua Teste',
        number: '42',
        complement: 'Sala 3',
        neighborhood: 'Centro',
        city: 'Curitiba',
        state: 'PR',
        cep: '80000-000',
      }));
    });
  });

  describe('closeForm()', () => {
    it('resets the form and hides it', () => {
      // Put component into editing state
      component.showForm.set(true);
      component.editingId.set('addr-1');
      component.form.patchValue({ street: 'Some street', number: '10' });

      component.closeForm();

      expect(component.showForm()).toBe(false);
      expect(component.editingId()).toBeNull();
      // Form controls should be reset (null values after reset)
      expect(component.form.get('street')!.value).toBeNull();
      expect(component.form.get('number')!.value).toBeNull();
    });
  });

  describe('deleteAddress()', () => {
    it('removes the correct address from the list', () => {
      const addresses = [
        { id: '1', street: 'Rua A', number: '1', complement: '', neighborhood: 'N1', city: 'C1', state: 'SP', cep: '00000-000' },
        { id: '2', street: 'Rua B', number: '2', complement: '', neighborhood: 'N2', city: 'C2', state: 'RJ', cep: '11111-111' },
        { id: '3', street: 'Rua C', number: '3', complement: '', neighborhood: 'N3', city: 'C3', state: 'MG', cep: '22222-222' },
      ];
      component.addresses.set(addresses);

      component.deleteAddress('2');

      const result = component.addresses();
      expect(result.length).toBe(2);
      expect(result.find(a => a.id === '2')).toBeUndefined();
      expect(result.find(a => a.id === '1')).toBeDefined();
      expect(result.find(a => a.id === '3')).toBeDefined();
    });
  });
});
