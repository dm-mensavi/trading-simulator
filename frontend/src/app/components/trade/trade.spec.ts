import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TradeComponent } from './trade';

describe('TradeComponent', () => {
  let component: TradeComponent;
  let fixture: ComponentFixture<TradeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TradeComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(TradeComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
