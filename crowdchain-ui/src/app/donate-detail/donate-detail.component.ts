// src/app/donate-detail/donate-detail.component.ts
import { Component, OnInit } from '@angular/core';
import {Form, FormBuilder, FormControl, FormGroup, FormGroupDirective, NgForm, Validators} from '@angular/forms';
import { DonateService } from '../donate.service';
import {ActivatedRoute, Router} from "@angular/router";
import {MatDatepicker} from "@angular/material/datepicker";
const ANONYMOUS = 'Anonymous';

@Component({
  selector: 'app-donate-detail',
  templateUrl: './donate-detail.component.html',
  styleUrls: ['./donate-detail.component.css']
})
export class DonateDetailComponent implements OnInit {
  donate: any;
  donateForm: FormGroup;
  name = ANONYMOUS;
  date: string;
  amount: number;

  constructor(private formBuilder: FormBuilder, private donateService: DonateService, private router: Router, private route: ActivatedRoute,) { }

  ngOnInit(): void {
    this.donateForm = this.formBuilder.group({
      name: ['', Validators.required],
      cardNumber: ['', [Validators.required, Validators.minLength(16), Validators.maxLength(16)]],
      expiryDate: ['', Validators.required],
      cvv: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(3)]],
      amount: ['', [Validators.required, Validators.min(1)]]
    });
  }

  onSubmit(f: NgForm): void {
    console.log(f.value);
    if (this.donateForm.valid) {
      this.donateService.donate(this.route.snapshot.paramMap.get("id"), f.value).subscribe(
        () => {
          this.router.navigate(['/thank-you']);
        }
      );
    }
  }

  formatLabel(value: number): string {
    if (value >= 1000) {
      return Math.round(value / 1000) + 'k';
    }

    return `${value}`;
  }

  protected readonly String = String;
}
