import { Component } from '@angular/core';

@Component({
  selector: 'app-login',
  standalone: true,
  template: `<div class="login-container"><h2>Entrar</h2></div>`,
  styles: [`.login-container { display: flex; justify-content: center; padding: 4rem; }`]
})
export class LoginComponent {}
