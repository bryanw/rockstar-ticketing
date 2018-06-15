import { TestBed, inject } from '@angular/core/testing';

import { AdobeIoService } from './adobe-io.service';

describe('AdobeIoService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [AdobeIoService]
    });
  });

  it('should be created', inject([AdobeIoService], (service: AdobeIoService) => {
    expect(service).toBeTruthy();
  }));
});
