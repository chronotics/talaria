package org.chronotics.talaria.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.chronotics.talaria.common.TaskExecutor.PROPAGATION_RULE;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
public class ExecutorTest {
	private static TaskExecutor<List<Integer>> finalExecutor = null;
	private static TaskExecutor<List<Integer>> increaseExecutor = null;
	private static TaskExecutor<List<Integer>> decreaseExecutor = null;
	
	@Rule
    public ExpectedException exceptions = ExpectedException.none();

	@BeforeClass
	public static void setUp() {
		finalExecutor = 
				new TaskExecutor<List<Integer>>(
						PROPAGATION_RULE.SIMULTANEOUSLY, 
						null) {
			@Override
			public List<Integer> call() throws Exception {
				List<Integer> v = this.getValue();
				List<Integer> rt = 
						v.stream()
						.map(e -> e)
						.collect((Collectors.toList()));
						return rt;
			}

			@Override
			public int getFutureTimeout() {
				// TODO Auto-generated method stub
				return 500;
			}
		};
		
		increaseExecutor = 
				new TaskExecutor<List<Integer>>(
						PROPAGATION_RULE.SIMULTANEOUSLY,
						null) {
					@Override
					public List<Integer> call() throws Exception {
						List<Integer> v = this.getValue();
						List<Integer> rt = 
								v.stream()
								.map(e -> e=e+1)
								.collect((Collectors.toList()));
								return rt;
					}

					@Override
					public int getFutureTimeout() {
						// TODO Auto-generated method stub
						return 500;
					}
		};		
		
		decreaseExecutor = 
				new TaskExecutor<List<Integer>>(
						PROPAGATION_RULE.SIMULTANEOUSLY,
						null) {
					@Override
					public List<Integer> call() throws Exception {
						List<Integer> v = this.getValue();
						List<Integer> rt = 
								v.stream()
								.map(e -> e=e-1)
								.collect((Collectors.toList()));
								return rt;
					}

					@Override
					public int getFutureTimeout() {
						// TODO Auto-generated method stub
						return 500;
					}
		};
	}

	@Test
	public void increase() {
		List<Integer> v = new ArrayList<Integer>();
		for(int i=0; i<100; i++) {
			v.add(i);
		}
		increaseExecutor.addNextExecutor(PROPAGATION_RULE.SIMULTANEOUSLY, null);
		try {
			Future<List<Integer>> future = increaseExecutor.execute(v);
			List<Integer> rt = future.get();
			assertEquals(rt.size(), 100);
			int expected = 1;
			for(Integer e : rt) {
//				System.out.println(e);
				assertEquals(expected, e.intValue());
				expected++;
			}	
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void decrease() {
		List<Integer> v = new ArrayList<Integer>();
		for(int i=0; i<100; i++) {
			v.add(i);
		}
		decreaseExecutor.addNextExecutor(PROPAGATION_RULE.SIMULTANEOUSLY, null);
		try {
			Future<List<Integer>> future = decreaseExecutor.execute(v);
			List<Integer> rt = future.get();
			assertEquals(rt.size(), 100);
			int expected = -1;
			for(Integer e : rt) {
//				System.out.println(e);
				assertEquals(expected, e.intValue());
				expected++;
			}	
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void increaseDecrease() {
		List<Integer> v = new ArrayList<Integer>();
		for(int i=0; i<100; i++) {
			v.add(i);
		}
		decreaseExecutor.addNextExecutor(PROPAGATION_RULE.STEP_BY_STEP_REGENERATED_ARG, null);
		increaseExecutor.addNextExecutor(PROPAGATION_RULE.STEP_BY_STEP_REGENERATED_ARG, decreaseExecutor);
		try {
			Future<List<Integer>> future = increaseExecutor.execute(v);
			List<Integer> rt = future.get();
			int expected = 0;
			for(Integer e : rt) {
//				System.out.println(e);
				assertEquals(expected, e.intValue());
				expected++;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void propagateExecutor() {
		List<Integer> v = new ArrayList<Integer>();
		for(int i=0; i<100; i++) {
			v.add(i);
		}
		finalExecutor.addNextExecutor(PROPAGATION_RULE.STEP_BY_STEP_REGENERATED_ARG, null);
		decreaseExecutor.addNextExecutor(PROPAGATION_RULE.STEP_BY_STEP_REGENERATED_ARG, finalExecutor);
		increaseExecutor.addNextExecutor(PROPAGATION_RULE.STEP_BY_STEP_REGENERATED_ARG, decreaseExecutor);
		
		try {
			Future<List<Integer>> future = increaseExecutor.execute(v);
			List<Integer> rt = future.get();
			int expected = 0;
			for(Integer e : rt) {
//				System.out.println(e);
				assertEquals(expected, e.intValue());
				expected++;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void propagateExecutorWithBuilder() {
		List<Integer> v = new ArrayList<Integer>();
		for(int i=0; i<100; i++) {
			v.add(i);
		}
		
		TaskExecutor<List<Integer>> executor = null;
		try {
			executor = new TaskExecutor.Builder<List<Integer>>()
			.setExecutor(increaseExecutor,PROPAGATION_RULE.STEP_BY_STEP_REGENERATED_ARG)
			.addExecutor(decreaseExecutor)
			.addExecutor(finalExecutor)
			.build();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try {
			Future<List<Integer>> future = executor.execute(v);
			List<Integer> rt = future.get();
			int expected = 0;
			for(Integer e : rt) {
//				System.out.println(e);
				assertEquals(expected, e.intValue());
				expected++;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void propagateExecutorWithBuilderException() {
		exceptions.expect(java.lang.AssertionError.class);
		
		List<Integer> v = new ArrayList<Integer>();
		for(int i=0; i<100; i++) {
			v.add(i);
		}
		
		TaskExecutor<List<Integer>> executor = null;
		try {
			executor = new TaskExecutor.Builder<List<Integer>>()
			.setExecutor(increaseExecutor,PROPAGATION_RULE.STEP_BY_STEP_REGENERATED_ARG)
			.addExecutor(decreaseExecutor)
			.addExecutor(decreaseExecutor)
			.build();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			assertTrue(e1 != null);
		}
	}
	
	@Test
	public void propagationRuleException() {
		exceptions.expect(IllegalStateException.class);

		finalExecutor.addNextExecutor(PROPAGATION_RULE.SIMULTANEOUSLY, null);
		decreaseExecutor.addNextExecutor(PROPAGATION_RULE.STEP_BY_STEP_REGENERATED_ARG, finalExecutor);
		increaseExecutor.addNextExecutor(PROPAGATION_RULE.STEP_BY_STEP_REGENERATED_ARG, decreaseExecutor);
	}
	
	@Test
	public void getChildrenExecutorCount() {
//		exceptions.expect(IllegalStateException.class);

		finalExecutor.addNextExecutor(PROPAGATION_RULE.STEP_BY_STEP_REGENERATED_ARG, null);
		decreaseExecutor.addNextExecutor(PROPAGATION_RULE.STEP_BY_STEP_REGENERATED_ARG, null);
		increaseExecutor.addNextExecutor(PROPAGATION_RULE.STEP_BY_STEP_REGENERATED_ARG, decreaseExecutor);
		
		assertEquals(increaseExecutor.getChildrenExecutorCount(),1);
	}
}
