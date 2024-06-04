import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DonateDetailComponent } from './donate-detail.component';

describe('DonateDetailComponent', () => {
  let component: DonateDetailComponent;
  let fixture: ComponentFixture<DonateDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [DonateDetailComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(DonateDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
