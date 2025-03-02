package com.labs.lab2;

public class Transition {
    private String state;
    private String input;

    public Transition(String state, String input) {
        this.state = state;
        this.input = input;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((state == null) ? 0 : state.hashCode());
        result = prime * result + ((input == null) ? 0 : input.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Transition other = (Transition) obj;
        if (state == null) {
            if (other.state != null)
                return false;
        } else if (!state.equals(other.state))
            return false;
        if (input == null) {
            if (other.input != null)
                return false;
        } else if (!input.equals(other.input))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "δ(" + this.state + ", " + this.input + ")";
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }
}
