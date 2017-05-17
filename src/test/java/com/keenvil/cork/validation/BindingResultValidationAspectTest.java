package com.keenvil.cork.validation;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collections;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.easymock.EasyMockRunner;
import org.easymock.TestSubject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.validation.BindingResult;

import com.keenvil.cork.error.KeenvilBusinessException;

@RunWith(EasyMockRunner.class)
public class BindingResultValidationAspectTest {

  @TestSubject
  private BindingResultValidationAspect aspect =
      new  BindingResultValidationAspect();


  @Test
  public void validateWithNoErrors() {
    JoinPoint point = createMock(JoinPoint.class);
    BindingResult result = createMock(BindingResult.class);
    Signature signature = createMock(Signature.class);
    
    expect(result.hasErrors()).andReturn(false).anyTimes();
    expect(point.getArgs()).andReturn(new Object[]{result});
    expect(point.getSignature()).andReturn(signature);
    expect(signature.getName()).andReturn("methodName");
    replay(point, result, signature);

    aspect.validate(point);
    verify(point, result, signature);
  }

  @Test
  public void validateWithErrors() {
    JoinPoint point = createMock(JoinPoint.class);
    BindingResult result = createMock(BindingResult.class);
    Signature signature = createMock(Signature.class);
    
    expect(result.hasErrors()).andReturn(true).anyTimes();
    expect(result.getAllErrors()).andReturn(Collections.emptyList());
    expect(point.getArgs()).andReturn(new Object[]{result});
    expect(point.getSignature()).andReturn(signature);
    expect(signature.getName()).andReturn("methodName");
    replay(point, result, signature);

    try {
      aspect.validate(point);
      fail();
    } catch (KeenvilBusinessException.ValidationError exception) {
      assertTrue(exception.getErrors().size() == 0);
    }
    verify(point, result, signature);
  }

  @Test
  public void validateWithNoBindingResultInstance() {
    JoinPoint point = createMock(JoinPoint.class);
    Signature signature = createMock(Signature.class);
    
    expect(point.getArgs()).andReturn(new Object[]{new Object()});
    expect(point.getSignature()).andReturn(signature);
    expect(signature.getName()).andReturn("methodName");
    replay(point, signature);

    aspect.validate(point);
    verify(point, signature);
  }
}
