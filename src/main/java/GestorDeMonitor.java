package com.unc.concurrente.monitor;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.unc.concurrente.rdp.RDP;
import com.unc.concurrente.rdp.RDPTemporal;
import com.unc.concurrente.utils.FileManagement;
import com.unc.concurrente.utils.ShootingStates;

public class GestorDeMonitor {
	private static final Logger LOG = LoggerFactory.getLogger(GestorDeMonitor.class); 
	private FileManagement manejador;
	private Semaphore mutex;
	private RDP rdp;
	private RDPTemporal rdpTemp;
	private Cola[] colas;
	private boolean k;
	
	public GestorDeMonitor(RDP rdp, FileManagement manejador) {
		this.rdp = rdp;
		this.mutex = new Semaphore(1, true);
		this.colas = new Cola[rdp.getTransiciones()];
		this.manejador = manejador;
		
		for(int i = 0; i < colas.length; i++){
			colas[i] = new Cola();
		}
	}
	
	public GestorDeMonitor(RDPTemporal rdpTemp, FileManagement manejador) {
		this.rdpTemp = rdpTemp;
		this.mutex = new Semaphore(1, true);
		this.colas = new Cola[rdpTemp.getTransiciones()];
		this.manejador = manejador;
		
		for(int i = 0; i < colas.length; i++){
			colas[i] = new Cola();
		}
	}
	
	public void dispararTransicionSinTiempo(int transicion) {
		try {
			mutex.acquire();
			LOG.info("%s: toma mutex, ingresa al monitor.", Thread.currentThread().getName());
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
		
		k = true;
		while(k == true) {
			LOG.info("{}: intenta disparar transicion {}.", Thread.currentThread().getName(), transicion);
			k = rdp.disparar(transicion); // disparar una transicion
			
			if(k == true) { //el hilo puede ejecutar su tarea
				LOG.info("{}: exito al disparar transicion {}.", Thread.currentThread().getName(), transicion);
				manejador.escribirTBuffer(transicion);
				manejador.escribirPBuffer(Arrays.toString(rdp.getM_actual()));
				Boolean[] vectorDeSensibilizadas = rdp.getSensibilizadasPorMarca();
				Boolean[] vectorDeColas = getVectorDeColas();
				Boolean[] m = operacionAnd(vectorDeSensibilizadas, vectorDeColas);
				
				if(contarSensibilizadas(m) != 0) {
					int numCola = Politica.cual(m, rdp.getM_actual());
					LOG.info("{}: se saca hilo de cola para disparar transicion {}, saliendo del monitor.",
							Thread.currentThread().getName(), numCola);
					colas[numCola].release();
					return;
				} else {
					LOG.info("{}: no hay hilos para despertar, saliendo del monitor.", Thread.currentThread().getName());
					k = false;
				}
			} else {
				LOG.info("{}: no se pudo dispàrar la transicion {}, prerado para encolarse.",
						Thread.currentThread().getName(), transicion);
				mutex.release();
				colas[transicion].acquire();//se pone al hilo en la cola de la transicion
			}
		}
		mutex.release();
		return;
	}
	
	public void dispararTransicion(int transicion) {
		try {
			mutex.acquire();
			LOG.info("{}: toma el mutex, ingresa al monitor.", Thread.currentThread().getName());
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
		
		k = true;
		while(k == true) {
			LOG.info("{}: intenta disparar transicion {}.", Thread.currentThread().getName(), transicion);
			ShootingStates estadoDeDisparo = rdpTemp.dispararRed(transicion); // disparar una transicion
			
			switch(estadoDeDisparo) {
			case SUCCESS: //Se disparo la transicion
				LOG.info("{}: exito al disparar transicion {}.", Thread.currentThread().getName(), transicion);
				manejador.escribirTBuffer(transicion);
				manejador.escribirPBuffer(rdpTemp.toString());
				Boolean[] vectorDeSensibilizadas = rdpTemp.getSensibilizadasPorMarca();
				Boolean[] vectorDeColas = getVectorDeColas();
				Boolean[] m = operacionAnd(vectorDeSensibilizadas, vectorDeColas);
				
				if(contarSensibilizadas(m) != 0) {
					Integer numCola = Politica.cual(m, rdpTemp.getM_actual());
					if(numCola == -1) {
						k = false;
					} else {
						LOG.info("{}: se saca hilo de cola para disparar transicion {}, saliendo del monitor.",
								Thread.currentThread().getName(), numCola);
						colas[numCola].release();
						return;
					}
				} else {
					LOG.info("{}: no hay hilos para despertar, saliendo del monitor.", Thread.currentThread().getName());
					k = false;
				}
				break;
			case FAIL: // no se pudo disparar la transicion
				LOG.info("{}: no se pudo disparar la transicion {}, preparado para encolarse.",
						Thread.currentThread().getName(), transicion);
				mutex.release();
				colas[transicion].acquire();//se pone al hilo en la cola de la transicion
				break;
			case EXIT: // despues del beta
				LOG.info("{}: intento disparar la transicion {} despues del beta, preparado para encolarse.",
						Thread.currentThread().getName(), transicion);
				mutex.release();
				colas[transicion].acquire();//se pone al hilo en la cola de la transicion
				break;
			case SLEEP: //debe hacer sleep para poder llegar al alfa
				try {
					LOG.info("{}: intento disparar la transicion {} antes del alfa, preparado para hacer sleep.",
							Thread.currentThread().getName(), transicion);
					mutex.release();
					TimeUnit.SECONDS.sleep(rdpTemp.getTimeStamp(transicion).longValue());
					mutex.acquire();
					k = true;
					LOG.info("{}: despierta del sleep, toma el mutex.", Thread.currentThread().getName());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				break;
			}
		}
		mutex.release();
		return;
	}
	
	Boolean[] getVectorDeColas() {
		Boolean[] vc = new Boolean[colas.length];
		
		for(int i = 0; i < colas.length; i++){
			vc[i] = colas[i].quienesEstan();
		}
		return vc;
	}
	
	Boolean[] operacionAnd( Boolean[] vectorUno, Boolean[] vectorDos) {
		if(Objects.isNull(vectorUno) || Objects.isNull(vectorDos)) {
			throw new NullPointerException();
		} else if(vectorUno.length != vectorDos.length) {
			throw new IllegalArgumentException();
		} else {
			Boolean[] vAnd = new Boolean[vectorUno.length];
			
			for(int i = 0; i < vAnd.length; i++) {
				vAnd[i] =Boolean.logicalAnd(vectorUno[i].booleanValue(),
						vectorDos[i].booleanValue());
			}
			return vAnd;
		}
	}
	
	public int contarSensibilizadas(Boolean[] a) {
		return Arrays.asList(a).stream().filter(d -> d == true).collect(Collectors.toList()).size();
	}
}
