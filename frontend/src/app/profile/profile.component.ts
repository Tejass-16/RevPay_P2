import { Component, OnInit } from '@angular/core';

import { FormsModule } from '@angular/forms';

import { ProfileService, UserProfile, WalletProfile, PasswordChangeRequest } from '../services/profile.service';

import { Router } from '@angular/router';

import { CommonModule } from '@angular/common';



@Component({

  selector: 'app-profile',

  templateUrl: './profile.component.html',

  styleUrls: ['./profile.component.css'],

  standalone: true,

  imports: [CommonModule, FormsModule]

})

export class ProfileComponent implements OnInit {

  profile: UserProfile = {

    id: 0,

    email: '',

    firstName: '',

    lastName: '',

    phone: '',

    businessAddress: '',

    role: '',

    createdAt: '',

    updatedAt: ''

  };



  accountNumber: string = '';

  editMode: boolean = false;

  isLoading: boolean = false;



  // Password change form

  passwordForm: PasswordChangeRequest = {

    currentPassword: '',

    newPassword: '',

    confirmPassword: ''

  };



  showPasswordForm: boolean = false;

  isChangingPassword: boolean = false;



  constructor(

    private profileService: ProfileService,

    private router: Router

  ) {}



  ngOnInit(): void {

    this.loadProfile();

  }



  loadProfile(): void {

    this.isLoading = true;

    this.profileService.getProfile().subscribe({

      next: (profile) => {

        this.profile = profile;

        this.isLoading = false;

      },

      error: (error) => {

        console.error('Error loading profile:', error);

        this.isLoading = false;

      }

    });

  }



  toggleEditMode(): void {

    this.editMode = !this.editMode;

    if (!this.editMode) {

      this.saveProfile();

    }

  }



  saveProfile(): void {

    this.isLoading = true;

    const updatedProfile = {

      firstName: this.profile.firstName,

      lastName: this.profile.lastName,

      phone: this.profile.phone,

      businessAddress: this.profile.businessAddress

    };



    this.profileService.updateProfile(updatedProfile).subscribe({

      next: () => {

        this.editMode = false;

        this.isLoading = false;

        alert('Profile updated successfully!');

      },

      error: (error) => {

        console.error('Error updating profile:', error);

        this.isLoading = false;

        alert('Failed to update profile');

      }

    });

  }



  togglePasswordForm(): void {

    this.showPasswordForm = !this.showPasswordForm;

    if (!this.showPasswordForm) {

      this.resetPasswordForm();

    }

  }



  changePassword(): void {

    if (this.passwordForm.newPassword !== this.passwordForm.confirmPassword) {

      alert('New passwords do not match');

      return;

    }



    if (this.passwordForm.newPassword.length < 6) {

      alert('Password must be at least 6 characters long');

      return;

    }



    this.isChangingPassword = true;

    this.profileService.changePassword(this.passwordForm).subscribe({

      next: () => {

        this.isChangingPassword = false;

        this.showPasswordForm = false;

        this.resetPasswordForm();

        alert('Password changed successfully!');

      },

      error: (error) => {

        console.error('Error changing password:', error);

        this.isChangingPassword = false;

        alert('Failed to change password: ' + (error.message || 'Unknown error'));

      }

    });

  }



  private resetPasswordForm(): void {

    this.passwordForm = {

      currentPassword: '',

      newPassword: '',

      confirmPassword: ''

    };

  }

}