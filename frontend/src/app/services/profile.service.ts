import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../environments/environment';






export interface UserProfile {



  id: number;



  email: string;



  firstName: string;



  lastName: string;



  phone?: string;



  businessAddress?: string;



  role: string;



  createdAt: string;



  updatedAt: string;



  isVerified?: boolean;



}







export interface WalletProfile {



  user: UserProfile;



  wallet: {



    balance: number;



    accountNumber: string;



    isActive: boolean;



    createdAt: string;



  };



}







export interface PasswordChangeRequest {



  currentPassword: string;



  newPassword: string;



  confirmPassword: string;



}







@Injectable({



  providedIn: 'root'



})



export class ProfileService {



  private apiUrl = environment.apiUrl;







  constructor(private http: HttpClient) {}







  // Get user profile



  getProfile(): Observable<UserProfile> {

    const token = localStorage.getItem('currentUser');

    const user = token ? JSON.parse(token) : null;

    const authToken = user ? user.token : null;

    

    const headers = authToken ? new HttpHeaders({

      'Content-Type': 'application/json',

      'Authorization': `Bearer ${authToken}`

    }) : new HttpHeaders({

      'Content-Type': 'application/json'
    });

    return this.http.get<any>(`${this.apiUrl}/wallet/profile`, { headers }).pipe(
      map((response: any) => {
        // Handle the nested response structure from backend
        if (response && response.user && response.wallet) {
          return {
            ...response.user,
            // Add account number from wallet data
            accountNumber: response.wallet.accountNumber || ''
          };
        } else if (response && typeof response === 'object') {
          // Handle direct object response (fallback)
          return {
            ...response,
            accountNumber: response.accountNumber || ''
          };
        } else {
          // Return empty profile if response is invalid
          return {
            id: 0,
            email: '',
            firstName: '',
            lastName: '',
            phone: '',
            businessAddress: '',
            role: '',
            createdAt: '',
            updatedAt: '',
            accountNumber: ''
          };
        }
      })
    );
  }




  // Get full wallet profile (includes account number)



  getWalletProfile(): Observable<WalletProfile> {



    const token = localStorage.getItem('currentUser');



    const user = token ? JSON.parse(token) : null;



    const authToken = user ? user.token : null;



    



    const headers = authToken ? new HttpHeaders({



      'Content-Type': 'application/json',



      'Authorization': `Bearer ${authToken}`



    }) : new HttpHeaders({



      'Content-Type': 'application/json'



    });







    return this.http.get<WalletProfile>(`${this.apiUrl}/wallet/profile`, { headers });



  }







  // Update user profile



  updateProfile(profile: Partial<UserProfile>): Observable<any> {



    const token = localStorage.getItem('currentUser');



    const user = token ? JSON.parse(token) : null;



    const authToken = user ? user.token : null;



    



    const headers = authToken ? new HttpHeaders({



      'Content-Type': 'application/json',



      'Authorization': `Bearer ${authToken}`



    }) : new HttpHeaders({



      'Content-Type': 'application/json'



    });







    return this.http.put(`${this.apiUrl}/profile`, profile, { headers });



  }







  // Change password



  changePassword(passwordRequest: PasswordChangeRequest): Observable<any> {



    const token = localStorage.getItem('currentUser');



    const user = token ? JSON.parse(token) : null;



    const authToken = user ? user.token : null;



    



    const headers = authToken ? new HttpHeaders({



      'Content-Type': 'application/json',



      'Authorization': `Bearer ${authToken}`



    }) : new HttpHeaders({



      'Content-Type': 'application/json'



    });







    return this.http.post(`${this.apiUrl}/profile/change-password`, passwordRequest, { headers });



  }



}