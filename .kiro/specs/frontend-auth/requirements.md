# Requirements Document

## Introduction

Frontend authentication system for the "Reino & Flor" storefront Angular application. This feature provides login, customer self-registration, password recovery, auth state management, protected route integration, and security controls. The implementation leverages the existing AuthPort, TokenStorageService, JWT interceptor, and auth guards already present in the infrastructure layer.

## Glossary

- **Auth_System**: The frontend authentication module responsible for managing user authentication lifecycle including login, registration, password recovery, token management, and session state.
- **Login_Page**: The page component that presents the email/password form for user authentication.
- **Register_Page**: The page component that presents the customer self-registration form.
- **Recovery_Page**: The page component for requesting a password reset link.
- **Auth_Service**: The Angular service managing reactive auth state via signals (currentUser, isAuthenticated, role) and coordinating token lifecycle.
- **Token_Refresh_Scheduler**: The mechanism within Auth_Service that schedules automatic token refresh before expiry.
- **Form_Validator**: The reactive form validation logic that enforces input constraints and provides error messages.
- **Route_Guard**: The existing CanActivateFn-based guard that protects routes requiring authentication or specific roles.
- **JWT_Interceptor**: The existing HTTP interceptor that attaches Bearer tokens to outgoing requests.
- **Token_Storage**: The existing TokenStorageService that persists tokens in sessionStorage or localStorage.
- **CPF**: Cadastro de Pessoa Física, Brazilian individual taxpayer registry number (11 digits).
- **returnUrl**: A query parameter capturing the originally requested URL for post-login redirection.

## Requirements

### Requirement 1: Login Form Submission

**User Story:** As a store user, I want to log in with my email and password, so that I can access protected areas of the application.

#### Acceptance Criteria

1. WHEN the user submits valid credentials, THE Login_Page SHALL send a login request to the Auth_System and navigate the user to the returnUrl or the default route for the user role.
2. WHILE the login request is in flight, THE Login_Page SHALL display a loading indicator and disable the submit button.
3. IF the Auth_System returns an invalid credentials error, THEN THE Login_Page SHALL display the message "E-mail ou senha inválidos" without revealing which field is incorrect.
4. IF the Auth_System returns an account locked error, THEN THE Login_Page SHALL display the message "Conta bloqueada. Tente novamente em X minutos" where X is the remaining lockout duration.
5. IF a network error occurs during login, THEN THE Login_Page SHALL display the message "Erro de conexão. Verifique sua internet e tente novamente."
6. WHEN the user checks the "Lembrar-me" option, THE Token_Storage SHALL persist tokens in localStorage instead of sessionStorage.

### Requirement 2: Login Form Validation

**User Story:** As a store user, I want immediate feedback on invalid form input, so that I can correct errors before submitting.

#### Acceptance Criteria

1. THE Form_Validator SHALL mark the email field as invalid when the value is empty or does not match a valid email format.
2. THE Form_Validator SHALL mark the password field as invalid when the value is empty or contains fewer than 8 characters.
3. WHEN the user interacts with a form field and leaves it in an invalid state, THE Login_Page SHALL display the corresponding error message below the field.
4. WHILE the form contains validation errors, THE Login_Page SHALL keep the submit button disabled.

### Requirement 3: Customer Self-Registration

**User Story:** As a new customer, I want to create an account, so that I can track my orders and access personalized features.

#### Acceptance Criteria

1. THE Register_Page SHALL present fields for full name, email, password, confirm password, phone, and an optional CPF field.
2. THE Form_Validator SHALL mark the password as invalid when the value contains fewer than 8 characters.
3. THE Form_Validator SHALL mark the confirm password field as invalid when the value does not match the password field.
4. WHEN the user types in the password field, THE Register_Page SHALL display a password strength indicator showing weak, medium, or strong classification.
5. THE Register_Page SHALL present a mandatory terms acceptance checkbox that the user must check before submitting.
6. IF the Auth_System returns a duplicate email error, THEN THE Register_Page SHALL display the message "Este e-mail já está cadastrado."
7. WHEN registration is successful, THE Auth_System SHALL automatically log in the user and redirect to the home page.

### Requirement 4: Password Recovery

**User Story:** As a user who forgot the password, I want to request a recovery link, so that I can regain access to my account.

#### Acceptance Criteria

1. WHEN the user clicks "Esqueceu a senha?" on the Login_Page, THE Auth_System SHALL navigate to the Recovery_Page.
2. THE Form_Validator SHALL mark the email field on the Recovery_Page as invalid when the value is empty or does not match a valid email format.
3. WHEN the user submits a valid email on the Recovery_Page, THE Auth_System SHALL display a confirmation message "Se este e-mail estiver cadastrado, enviaremos um link de recuperação" regardless of whether the email exists.
4. AFTER displaying the confirmation message, THE Recovery_Page SHALL present a link to return to the Login_Page.

### Requirement 5: Auth State Management

**User Story:** As a developer, I want reactive auth state accessible throughout the application, so that components can respond to authentication changes.

#### Acceptance Criteria

1. THE Auth_Service SHALL expose signals for currentUser, isAuthenticated, and userRole that update synchronously when auth state changes.
2. WHEN login is successful, THE Auth_Service SHALL decode the JWT access token, extract the user information, and update the currentUser and userRole signals.
3. WHEN the access token reaches 80% of its expiresIn duration, THE Token_Refresh_Scheduler SHALL request a new token pair using the refresh token.
4. IF the token refresh fails, THEN THE Auth_Service SHALL clear all tokens, set isAuthenticated to false, and navigate to the Login_Page with a session expiry message.
5. WHEN logout is triggered, THE Auth_Service SHALL call the logout endpoint, clear all stored tokens, reset all auth signals, and navigate to the home page.

### Requirement 6: Protected Routes Integration

**User Story:** As a store user, I want to be redirected appropriately based on my authentication status, so that I access only authorized content.

#### Acceptance Criteria

1. WHEN an unauthenticated user attempts to access a protected route, THE Route_Guard SHALL redirect to the Login_Page with the original URL stored as the returnUrl query parameter.
2. WHEN login is successful and a returnUrl is present, THE Auth_System SHALL navigate to the returnUrl.
3. WHEN login is successful and no returnUrl is present, THE Auth_System SHALL navigate to the default route based on the user role.
4. WHILE the user is authenticated, THE Auth_System SHALL make role information available so that navigation components show or hide menu items based on the userRole signal.
5. WHEN an authenticated user accesses a route without the required role, THE Auth_System SHALL display a friendly "Acesso não autorizado" message page.

### Requirement 7: Security Controls

**User Story:** As a security-conscious user, I want my credentials and session to be protected, so that my account remains secure.

#### Acceptance Criteria

1. THE Login_Page SHALL present a show/hide toggle on the password field that switches between masked and visible text.
2. THE Auth_System SHALL transmit authentication credentials only in the request body, with no sensitive data in URL query parameters or path segments.
3. IF the Auth_System receives a rate-limiting response (HTTP 429), THEN THE Login_Page SHALL display "Muitas tentativas. Tente novamente em X minutos" with the retry duration extracted from the response.
4. THE Register_Page SHALL present a show/hide toggle on both the password and confirm password fields.
5. THE JWT_Interceptor SHALL exclude login and refresh endpoints from token attachment to prevent credential leakage.

### Requirement 8: Accessibility

**User Story:** As a user relying on assistive technology, I want the authentication forms to be fully accessible, so that I can complete login and registration tasks independently.

#### Acceptance Criteria

1. THE Login_Page SHALL associate each form input with a visible label element using matching for and id attributes.
2. WHEN a validation error is displayed, THE Form_Validator SHALL announce the error to screen readers using an aria-live="assertive" region.
3. WHEN the Login_Page loads, THE Auth_System SHALL move keyboard focus to the first form input.
4. WHEN a submission error occurs, THE Login_Page SHALL move keyboard focus to the error summary region.
5. THE Login_Page SHALL allow complete form submission using only the keyboard (Tab navigation, Enter to submit).
6. THE Register_Page SHALL meet the same label, aria-live, and focus management requirements as the Login_Page.

### Requirement 9: Visual Design Compliance

**User Story:** As the brand owner, I want authentication pages to reflect the Reino & Flor premium visual identity, so that customers experience a consistent brand.

#### Acceptance Criteria

1. THE Login_Page SHALL use the Playfair Display font for headings and the Inter font for body text and form labels.
2. THE Login_Page SHALL apply the brand gold (#C6A052) color for primary action buttons and active state indicators.
3. THE Login_Page SHALL render correctly on viewports from 320px width (mobile-first) up to 1440px (desktop).
4. THE Register_Page SHALL follow the same typography, color, and responsive rules as the Login_Page.
5. WHILE a form input has focus, THE Auth_System SHALL display a visible focus ring with sufficient contrast ratio (minimum 3:1 against adjacent colors) per WCAG 2.1 AA.

### Requirement 10: Automated Testing

**User Story:** As a developer, I want comprehensive test coverage for authentication features, so that regressions are caught early.

#### Acceptance Criteria

1. THE Auth_Service SHALL have unit tests that verify signal state transitions for login, logout, token refresh success, and token refresh failure scenarios.
2. THE Login_Page SHALL have component tests that verify form validation rules, error message display, and loading state behavior.
3. THE Register_Page SHALL have component tests that verify password strength indicator logic and duplicate email error handling.
4. THE Auth_System SHALL have integration tests that verify the complete login flow from form submission through token storage to route navigation.
