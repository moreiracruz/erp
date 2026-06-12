# Feature: Área do Cliente (Minha Conta)

## Overview
Provide authenticated customers with a self-service account area where they can manage their profile, view order history, manage delivery addresses, and change their password.

## Requirements

### 1. Profile Page
- 1.1 Display user information: full name, email, phone, CPF (masked as ***.***.***-XX)
- 1.2 Allow toggling into edit mode to update full name and phone
- 1.3 Email field is read-only in edit mode
- 1.4 Save changes via service call with loading state

### 2. Order History
- 2.1 Display a list of past orders with order number, date, status, and total
- 2.2 Status badges: Processando (gold), Enviado (olive), Entregue (brown)
- 2.3 Expand order to show item details (product name, quantity, price)
- 2.4 Show empty state message when no orders exist

### 3. Address Management
- 3.1 Display list of saved addresses as cards
- 3.2 Each card shows: street, number, complement, neighborhood, city, state, CEP
- 3.3 Provide add, edit, and delete actions for addresses
- 3.4 Inline form for adding/editing addresses with validation

### 4. Change Password
- 4.1 Form with current password, new password, and confirm new password fields
- 4.2 Validation: minimum 8 characters, new password and confirmation must match
- 4.3 Display success message on successful submission

### 5. Account Layout
- 5.1 Sidebar navigation on desktop (240px width) with links to all sections
- 5.2 Horizontal tab navigation on mobile
- 5.3 Gold active indicator on current section
- 5.4 Section title "Minha Conta" using Playfair Display font
