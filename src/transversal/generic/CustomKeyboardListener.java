package transversal.generic;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.lang.Boolean;
import javafx.scene.input.KeyCode;

@SuppressWarnings("hiding")
public class CustomKeyboardListener<KeyCode, Boolean> implements Map<KeyCode, Boolean> {

    private final HashMap<KeyCode, Boolean> delegatee;

    public CustomKeyboardListener(HashMap<KeyCode, Boolean> delegatee) {
        this.delegatee = delegatee;
    }

	@Override
	public void clear() {
		delegatee.clear();
	}

	@Override
	public boolean containsKey(Object key) {
		return delegatee.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return delegatee.containsValue(value);
	}

	@Override
	public Set<Entry<KeyCode, Boolean>> entrySet() {
		return delegatee.entrySet();
	}

	@Override
	public Boolean get(Object key) {
		Boolean ret = delegatee.get(key);
		if(ret!=null) {
			return ret;
		}
		return delegatee.get(javafx.scene.input.KeyCode.COLORED_KEY_3);
	}

	@Override
	public boolean isEmpty() {
		return delegatee.isEmpty();
	}

	@Override
	public Set<KeyCode> keySet() {
		return delegatee.keySet();
	}

	@Override
	public Boolean put(KeyCode key, Boolean value) {
		Boolean ret = delegatee.put(key, value);
		try {
			if(key.equals(javafx.scene.input.KeyCode.DOWN) && (boolean) delegatee.get(javafx.scene.input.KeyCode.UP)) {
				delegatee.put((KeyCode) javafx.scene.input.KeyCode.UP,null);
				;
			}
			if(key.equals(javafx.scene.input.KeyCode.UP) && (boolean) delegatee.get(javafx.scene.input.KeyCode.DOWN)) {
				delegatee.put((KeyCode) javafx.scene.input.KeyCode.DOWN,null);
				;
			}
		}catch(Exception V) {
			
		}
		
		return ret;
	}

	@Override
	public void putAll(Map<? extends KeyCode, ? extends Boolean> m) {
		delegatee.putAll(m);
		
	}

	@Override
	public Boolean remove(Object key) {
		return delegatee.remove(key);
	}

	@Override
	public int size() {
		return delegatee.size();
	}

	@Override
	public Collection<Boolean> values() {
		return delegatee.values();
	}
    
}