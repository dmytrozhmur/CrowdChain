import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DonateSuccessComponent } from './donate-success.component';

describe('DonateSuccessComponent', () => {
  let component: DonateSuccessComponent;
  let fixture: ComponentFixture<DonateSuccessComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [DonateSuccessComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(DonateSuccessComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
