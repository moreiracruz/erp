# Frontend Checkout — Requirements

## 1. Cart Page
- 1.1 Display a list of cart items showing product image, name, size, color, unit price, and quantity controls (+/- buttons)
- 1.2 Allow removing items from the cart via a remove button
- 1.3 Show the cart subtotal (sum of price × quantity for each item)
- 1.4 Display a "Finalizar Compra" gold CTA button that navigates to the checkout page
- 1.5 Show an empty cart state with "Seu carrinho está vazio" message and a link to the catalog

## 2. Checkout Flow
- 2.1 Implement a 3-step checkout flow: Dados Pessoais → Endereço → Pagamento
- 2.2 Show a step indicator at the top showing current and completed steps
- 2.3 Step 1 collects name, email, and phone (pre-filled if user is authenticated)
- 2.4 Step 2 collects delivery address: CEP, street, number, complement, neighborhood, city, state
- 2.5 Step 3 allows payment method selection: PIX, Cartão de Crédito, Cartão de Débito, Dinheiro

## 3. Order Summary
- 3.1 Display a compact order summary showing all items, subtotal, shipping cost, and total
- 3.2 Shipping is free for orders >= R$299; otherwise R$15.90
- 3.3 On desktop: show as a sidebar; on mobile: show as a collapsible section

## 4. Guest Checkout
- 4.1 Allow unauthenticated users to complete checkout without requiring login

## 5. Responsiveness
- 5.1 Cart page uses single column layout on mobile
- 5.2 Checkout page adapts step forms and order summary for mobile viewports

## 6. CartService Enhancement
- 6.1 Provide an updateQuantity method that changes an item's quantity
- 6.2 Expose computed signals for subtotal, shippingCost, and total
