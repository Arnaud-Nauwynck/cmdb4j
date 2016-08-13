package org.cmdb4j.core.ext.elasticsearch.filter;

public enum SearchHitFilterDecision {

	ACCEPT, REJECT, UNKNOWN;

	public SearchHitFilterDecision inverse() {
		switch(this) {
		case ACCEPT: return REJECT;
		case REJECT: return ACCEPT;
		case UNKNOWN: return UNKNOWN;
		default: return UNKNOWN;
		}
	}
	
	public SearchHitFilterDecision and(SearchHitFilterDecision other) {
		switch(this) {
		case ACCEPT: 
			switch(other){
			case ACCEPT: return ACCEPT;
			case REJECT: return REJECT;
			case UNKNOWN: return UNKNOWN; // ?? ACCEPT&&UNKONWN=ACCEPT  or UNKOWN??
			default: return UNKNOWN;
			}
		case REJECT: return REJECT;
		case UNKNOWN: return other;
		default: return UNKNOWN;
		}
		
	}

	public SearchHitFilterDecision or(SearchHitFilterDecision other) {
		switch(this) {
		case ACCEPT: return ACCEPT;
		case REJECT: 
			switch(other){
			case ACCEPT: return ACCEPT;
			case REJECT: return REJECT;
			case UNKNOWN: return UNKNOWN; // ?? REJECT||UNKONWN=UNKNOWN  or REJECT ??
			default: return UNKNOWN;
			}
		case UNKNOWN: return other;
		default: return UNKNOWN;
		}
	}

	public boolean isAcceptOrUnkown() {
		switch(this) {
		case ACCEPT: case UNKNOWN: return true;
		case REJECT: return false;
		default: return false;
		}
	}

	public boolean isRejectOrUnkown() {
		switch(this) {
		case REJECT: case UNKNOWN: return true;
		case ACCEPT: return false;
		default: return false;
		}
	}

}
